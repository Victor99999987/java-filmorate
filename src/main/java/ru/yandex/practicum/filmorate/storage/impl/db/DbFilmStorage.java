package ru.yandex.practicum.filmorate.storage.impl.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationFilmException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Slf4j
@Component
@Qualifier("DbFilmStorage")
public class DbFilmStorage extends DbStorage implements Storage<Film> {

    public DbFilmStorage(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public List<Film> getAll() {
        String sql = "select f.id, f.name, f.description, f.releasedate, f.duration, " +
                "f.mpa_id, m.name as mpa_name, fg.genres_id, g.name as genres_name, l.users_id \n" +
                "from films as f \n" +
                "left join mpa as m on f.mpa_id = m.id\n" +
                "left join films_genres as fg on fg.films_id = f.id\n" +
                "left join genres as g on fg.genres_id = g.id\n" +
                "left join likes as l on l.films_id = f.id";
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql);
        return makeFilms(sqlRowSet);
    }

    @Override
    public Film getById(Long id) {
        String sql = "select f.id, f.name, f.description, f.releasedate, f.duration, " +
                "f.mpa_id, m.name as mpa_name, fg.genres_id, g.name as genres_name, l.users_id \n" +
                "from films as f \n" +
                "left join mpa as m on f.mpa_id = m.id\n" +
                "left join films_genres as fg on fg.films_id = f.id\n" +
                "left join genres as g on fg.genres_id = g.id\n" +
                "left join likes as l on l.films_id = f.id where f.id = ?";
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql, id);
        if (!sqlRowSet.first()) {
            log.info(String.format("FilmNotFoundException: Не найден фильм с id=%d", id));
            throw new FilmNotFoundException(String.format("Не найден фильм с id=%d", id));
        }
        sqlRowSet.beforeFirst();
        return makeFilms(sqlRowSet).get(0);
    }

    @Override
    public Film add(Film film) {
        if (film.getId() != null) {
            log.info("ValidationFilmException: При добавлении фильма id должен быть null");
            throw new ValidationFilmException("При добавлении фильма id должен быть null");
        }
        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
        String sql = "insert into films (name, description, releaseDate, duration, mpa_id) values(?, ?, ?, ?, ?)";
        String finalSql = sql;
        jdbcTemplate.update(conn -> {
            PreparedStatement preparedStatement = conn.prepareStatement(finalSql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, film.getName());
            preparedStatement.setString(2, film.getDescription());
            preparedStatement.setDate(3, Date.valueOf(film.getReleaseDate()));
            preparedStatement.setInt(4, film.getDuration());
            preparedStatement.setLong(5, film.getMpa().getId());
            return preparedStatement;
        }, generatedKeyHolder);
        film.setId(generatedKeyHolder.getKey().longValue());

        sql = "insert into films_genres (films_id, genres_id) values(?, ?)";
        jdbcTemplate.batchUpdate(sql, film.getGenres(), film.getGenres().size(),
                (ps, genre) -> {
                    ps.setLong(1, film.getId());
                    ps.setLong(2, genre.getId());
                });

        return film;
    }

    @Override
    public Film remove(Long id) {
        Film film = getById(id);
        String sql = "delete from films where id = ?";
        jdbcTemplate.update(sql, id);
        return film;
    }

    @Override
    public Film update(Film film) {
        getById(film.getId());
        String sql = "update films set name = ?, description = ?, releaseDate = ?, duration = ?, mpa_id = ? where id = ?";
        jdbcTemplate.update(sql, film.getName(), film.getDescription(), Date.valueOf(film.getReleaseDate()),
                film.getDuration(), film.getMpa().getId(), film.getId());
        sql = "delete from films_genres where films_id = ?";
        jdbcTemplate.update(sql, film.getId());

        sql = "insert into films_genres (films_id, genres_id) values(?, ?)";
        jdbcTemplate.batchUpdate(sql, film.getGenres(), film.getGenres().size(),
                (ps, genre) -> {
                    ps.setLong(1, film.getId());
                    ps.setLong(2, genre.getId());
                });

        sql = "delete from likes where films_id = ?";
        jdbcTemplate.update(sql, film.getId());

        sql = "insert into likes (films_id, users_id) values(?, ?)";
        jdbcTemplate.batchUpdate(sql, film.getLikes(), film.getLikes().size(),
                (ps, usersId) -> {
                    ps.setLong(1, film.getId());
                    ps.setLong(2, usersId);
                });

        return film;
    }

    private List<Film> makeFilms(SqlRowSet rs) {
        Map<Long, Film> films = new HashMap<>();
        while (rs.next()) {
            Long id = rs.getLong("id");

            if (!films.containsKey(id)) {
                Mpa mpa = new Mpa(rs.getLong("mpa_id"), rs.getString(7));//"mpa_name"

                Film film = Film.builder()
                        .id(rs.getLong("id"))
                        .name(rs.getString("name"))
                        .description(rs.getString("description"))
                        .releaseDate(rs.getDate("releaseDate").toLocalDate())
                        .duration(rs.getInt("duration"))
                        .mpa(mpa)
                        .build();
                films.put(id, film);
            }

            Long genresId = rs.getLong("genres_id");
            if (!rs.wasNull()) {
                Genre genre = new Genre(genresId, rs.getString(9));//"genres_name"
                films.get(id).getGenres().add(genre);
            }

            Long usersId = rs.getLong("users_id");
            if (!rs.wasNull()) {
                films.get(id).getLikes().add(usersId);
            }
        }
        return new ArrayList<>(films.values());
    }

    public List<Film> getMovieRecommendations(Integer userId) {
        String sql = "" +
                "SELECT tb1.id, tb1.name, tb1.description, tb1.releasedate, tb1.duration, tb1.mpa_id, m.name as mpa_name" +
                ", fg.genres_id, g.name as genres_name, tb1.users_id " +
                "FROM (" +
                "   SELECT f.*, users_id" + //получили информацию о рекомендуемых фильмах
                "   FROM likes " +
                "   INNER JOIN films f ON films_id = f.id " + //присоединили к id фильма информацию о нем
                "   WHERE users_id IN (" +
                "       SELECT users_id " + //получили id пользователей с максимальным пересечением по лайкам
                "       FROM (" +
                "           SELECT users_id, COUNT(films_id) AS amount " +
                "           FROM likes " +
                "           WHERE films_id IN (" +
                "               SELECT DISTINCT films_id " +
                "               FROM likes " +
                "               WHERE users_id = ?" +
                "           ) AND users_id != ? " + //исключили пользователя из выборки
                "           GROUP BY users_id " +
                "           ORDER BY amount DESC) " +
                "       WHERE amount = (" +
                "           SELECT MAX(amount) AS max_intersection_by_likes " + //получили значение максимального
                "           FROM (" +                                           //пересечения по лайкам
                "               SELECT COUNT(films_id) AS amount " +
                "               FROM likes " +
                "               WHERE films_id IN (" +
                "                   SELECT DISTINCT films_id " + //получили id фильмов, которым пользователь поставил лайк
                "                   FROM likes " +
                "                   WHERE users_id = ?" +
                "               ) AND users_id != ? " + //исключили пользователя из выборки
                "               GROUP BY users_id" +
                "           )" +
                "       )" +
                "   ) AND films_id NOT IN (SELECT films_id FROM likes WHERE users_id = ?)" +
                ") AS tb1 " +
                "LEFT JOIN mpa AS m ON tb1.mpa_id = m.id " + //присоединили данные из таблицы mpa
                "LEFT JOIN films_genres AS fg ON tb1.id = fg.films_id " +
                "LEFT JOIN genres AS g ON g.id = fg.genres_id"; //присоединили данные из таблицы genres

        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql, userId, userId, userId, userId, userId);

        return makeFilms(sqlRowSet);
    }

}
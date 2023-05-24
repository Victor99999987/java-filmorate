package ru.yandex.practicum.filmorate.storage.impl.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DirectorNotFoundException;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationFilmException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Slf4j
@Component
@Qualifier("DbFilmStorage")
public class DbFilmStorage extends DbStorage implements FilmStorage {

    private final DbDirectorStorage directorStorage;

    public DbFilmStorage(JdbcTemplate jdbcTemplate, DbDirectorStorage directorStorage) {
        super(jdbcTemplate);
        this.directorStorage = directorStorage;
    }

    @Override
    public List<Film> getAll() {
        String sql = "select f.id, f.name, f.description, f.releasedate, f.duration, " +
                "f.mpa_id, m.name as mpa_name, fg.genres_id, g.name as genres_name, l.users_id,\n" +
                "dr.director_id as director_id, dr.name as DIRECTOR_NAME \n" +
                "from films as f \n" +
                "left join mpa as m on f.mpa_id = m.id\n" +
                "left join films_genres as fg on fg.films_id = f.id\n" +
                "left join genres as g on fg.genres_id = g.id\n" +
                "left join likes as l on l.films_id = f.id\n" +
                "left join film_director fd on f.id=fd.film_id\n" +
                "left join directors dr on fd.director_id=dr.DIRECTOR_ID";
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql);

        return makeFilms(sqlRowSet);
    }


    @Override
    public Film getById(Long id) {
        String sql = "select f.id, f.name, f.description, f.releasedate, f.duration, " +
                "f.mpa_id, m.name as mpa_name, fg.genres_id, g.name as genres_name, l.users_id,\n" +
                "d.director_id as sirector_id, d.name as director_name\n" +
                "from films as f \n" +
                "left join mpa as m on f.mpa_id = m.id\n" +
                "left join films_genres as fg on fg.films_id = f.id\n" +
                "left join genres as g on fg.genres_id = g.id\n" +
                "left join likes as l on l.films_id = f.id\n" +
                "left join film_director fd on f.id=fd.film_id\n" +
                "left join directors d on fd.director_id=d.DIRECTOR_ID\n" +
                "where f.id = ?";
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
        for (Director director : film.getDirectors()) {
            if (directorStorage.getById(director.getId()) == null) {
                log.info("Режиссер с не найден");
                throw new DirectorNotFoundException("Режиссёр не найден.");
            }
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

        sql = "delete from film_director where film_id = ?";
        jdbcTemplate.update(sql, film.getId());

        sql = "insert into film_director (film_id, director_id) values(?, ?)";
        jdbcTemplate.batchUpdate(sql, film.getDirectors(), film.getDirectors().size(),
                (ps, director) -> {
                    ps.setLong(1, film.getId());
                    ps.setLong(2, director.getId());
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

        sql = "delete from film_director where film_id = ?";
        jdbcTemplate.update(sql, film.getId());

        Set<Director> directors = film.getDirectors();
        if (film.getDirectors() != null) {
            for (Director director : directors) {
                jdbcTemplate.update("INSERT INTO FILM_DIRECTOR (FILM_ID, DIRECTOR_ID) VALUES (?,?)",
                        film.getId(), director.getId());
            }
        }
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

            Long directorId = rs.getLong("director_id");
            if (!rs.wasNull()) {
                Director director = new Director(directorId, rs.getString("DIRECTOR_NAME"));
                films.get(id).getDirectors().add(director);
            }

            Long usersId = rs.getLong("users_id");
            if (!rs.wasNull()) {
                films.get(id).getLikes().add(usersId);
            }
        }
        return new ArrayList<>(films.values());
    }

    @Override
    public List<Film> findFilmsSortByLikesAndYear(Long directorId, String param) {
        Integer count = jdbcTemplate.queryForObject("SELECT count(DIRECTOR_ID) FROM DIRECTORS WHERE DIRECTOR_ID = ?",
                Integer.class,
                directorId);

        if (count == null || count == 0) {
            throw new DirectorNotFoundException("Director with id='" + directorId + "' not found");
        }

        switch (param) {
            case "noParam":
                String sqlNoParam = "select f.id, f.name, f.description, f.releasedate, f.duration, " +
                        "f.mpa_id, m.name as mpa_name, fg.genres_id, g.name as genres_name, l.users_id,\n" +
                        "dr.director_id as director_id, dr.name as DIRECTOR_NAME \n" +
                        "from films as f \n" +
                        "left join mpa as m on f.mpa_id = m.id\n" +
                        "left join films_genres as fg on fg.films_id = f.id\n" +
                        "left join genres as g on fg.genres_id = g.id\n" +
                        "left join likes as l on l.films_id = f.id\n" +
                        "left join film_director fd on f.id=fd.film_id\n" +
                        "left join directors dr on fd.director_id=dr.DIRECTOR_ID\n" +
                        "WHERE dr.DIRECTOR_ID = ?";
                SqlRowSet sqlRowSetNoParam = jdbcTemplate.queryForRowSet(sqlNoParam, directorId);

                return makeFilms(sqlRowSetNoParam);

            case "year":
                String sqlYear = "select f.id, f.name, f.description, f.releasedate, f.duration, " +
                        "f.mpa_id, m.name as mpa_name, fg.genres_id, g.name as genres_name, l.users_id,\n" +
                        "dr.director_id as director_id, dr.name as DIRECTOR_NAME \n" +
                        "from films as f \n" +
                        "left join mpa as m on f.mpa_id = m.id\n" +
                        "left join films_genres as fg on fg.films_id = f.id\n" +
                        "left join genres as g on fg.genres_id = g.id\n" +
                        "left join likes as l on l.films_id = f.id\n" +
                        "left join film_director fd on f.id=fd.film_id\n" +
                        "left join directors dr on fd.director_id=dr.DIRECTOR_ID\n" +
                        "WHERE dr.DIRECTOR_ID = ?\n" +
                        "ORDER BY (f.RELEASEDATE)";

                SqlRowSet sqlRowSetYear = jdbcTemplate.queryForRowSet(sqlYear, directorId);
                List<Film> films = makeFilms(sqlRowSetYear);
                Collections.reverse(films);
                return films;

            case "likes":
                String sqlLikes = "select f.id, f.name, f.description, f.releasedate, f.duration, " +
                        "f.mpa_id, m.name as mpa_name, fg.genres_id, g.name as genres_name, l.users_id,\n" +
                        "dr.director_id as director_id, dr.name as DIRECTOR_NAME, count(l.USERS_ID) as likes \n" +
                        "from films as f \n" +
                        "left join mpa as m on f.mpa_id = m.id\n" +
                        "left join films_genres as fg on fg.films_id = f.id\n" +
                        "left join genres as g on fg.genres_id = g.id\n" +
                        "left join likes as l on l.films_id = f.id\n" +
                        "left join film_director fd on f.id=fd.film_id\n" +
                        "left join directors dr on fd.director_id=dr.DIRECTOR_ID\n" +
                        "WHERE dr.DIRECTOR_ID = ?\n" +
                        "GROUP BY f.ID " +
                        "ORDER BY likes";

                SqlRowSet sqlRowSetLikes = jdbcTemplate.queryForRowSet(sqlLikes, directorId);
                return makeFilms(sqlRowSetLikes);

        }

        return List.of();
    }
}

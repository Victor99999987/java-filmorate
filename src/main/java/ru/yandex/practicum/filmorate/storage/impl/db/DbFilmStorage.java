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
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Qualifier("DbFilmStorage")
public class DbFilmStorage implements Storage<Film> {

    private final JdbcTemplate jdbcTemplate;

    public DbFilmStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Film> getAll() {
        List<Film> result = new ArrayList<>();
        String sql = "select films.*, mpa.name as mpa_name from films join mpa on films.mpa_id = mpa.id";
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql);
        while (sqlRowSet.next()) {
            result.add(makeFilm(sqlRowSet));
        }
        return result;
    }

    private Film makeFilm(SqlRowSet rs) {
        Mpa mpa = new Mpa(rs.getLong("mpa_id"), rs.getString(7));

        Film film = Film.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("releaseDate").toLocalDate())
                .duration(rs.getInt("duration"))
                .mpa(mpa)
                .build();

        String sql = "select g.* from genres as g join films_genres as fg on fg.genres_id = g.id where fg.films_id = ?";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, film.getId());
        while (rowSet.next()) {
            film.getGenres().add(new Genre(rowSet.getLong("id"), rowSet.getString("name")));
        }

        sql = "select likes.users_id from likes where films_id = ?";
        rowSet = jdbcTemplate.queryForRowSet(sql, film.getId());
        while (rowSet.next()) {
            film.getLikes().add(rowSet.getLong("users_id"));
        }

        return film;
    }

    @Override
    public Film getById(Long id) {
        String sql = "select films.*, mpa.name as mpa_name from films join mpa on films.mpa_id=mpa.id where films.id = ?";
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql, id);
        if (!sqlRowSet.first()) {
            log.info(String.format("FilmNotFoundException: Не найден фильм с id=%d", id));
            throw new FilmNotFoundException(String.format("Не найден фильм с id=%d", id));
        }
        return makeFilm(sqlRowSet);
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
        for (Genre genre : film.getGenres()) {
            sql = "insert into films_genres (films_id, genres_id) values(?, ?)";
            jdbcTemplate.update(sql, film.getId(), genre.getId());
        }
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
        for (Genre genre : film.getGenres()) {
            sql = "insert into films_genres (films_id, genres_id) values(?, ?)";
            jdbcTemplate.update(sql, film.getId(), genre.getId());
        }
        sql = "delete from likes where films_id = ?";
        jdbcTemplate.update(sql, film.getId());
        for (Long like : film.getLikes()) {
            sql = "insert into likes (films_id, users_id) values(?, ?)";
            jdbcTemplate.update(sql, film.getId(), like);
        }
        return film;
    }
}

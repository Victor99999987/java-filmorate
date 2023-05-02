package ru.yandex.practicum.filmorate.storage.impl.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.GenreNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationGenreException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Qualifier("DbGenreStorage")
public class DbGenreStorage extends DbStorage implements Storage<Genre> {
    public DbGenreStorage(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public List<Genre> getAll() {
        List<Genre> result = new ArrayList<>();
        String sql = "select id, name from genres";
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql);
        while (sqlRowSet.next()) {
            result.add(makeGenre(sqlRowSet));
        }
        return result;
    }

    @Override
    public Genre getById(Long id) {
        String sql = "select id, name from genres where id = ?";
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql, id);
        if (!sqlRowSet.first()) {
            log.info(String.format("GenreNotFoundException: Не найден жанр с id=%d", id));
            throw new GenreNotFoundException(String.format("Не найден жанр с id=%d", id));
        }
        return makeGenre(sqlRowSet);
    }

    @Override
    public Genre add(Genre genre) {
        if (genre.getId() != null) {
            log.info("ValidationGenreException: При добавлении жанра id должен быть null");
            throw new ValidationGenreException("При добавлении жанра id должен быть null");
        }
        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
        String sql = "insert into genres (name) values(?)";
        jdbcTemplate.update(conn -> {
            PreparedStatement preparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, genre.getName());
            return preparedStatement;
        }, generatedKeyHolder);
        genre.setId(generatedKeyHolder.getKey().longValue());
        return genre;
    }

    @Override
    public Genre remove(Long id) {
        Genre genre = getById(id);
        String sql = "delete from genres where id = ?";
        jdbcTemplate.update(sql, id);
        return genre;
    }

    @Override
    public Genre update(Genre genre) {
        getById(genre.getId());
        String sql = "update genres set name = ? where id = ?";
        jdbcTemplate.update(sql, genre.getName(), genre.getId());
        return genre;
    }

    private Genre makeGenre(SqlRowSet rs) {
        return Genre.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .build();
    }
}

package ru.yandex.practicum.filmorate.storage.impl.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DirectorNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@Qualifier("DbDirectorStorage")
@Slf4j
public class DbDirectorStorage extends DbStorage implements Storage<Director> {

    public DbDirectorStorage(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public Director add(Director director) {
        String sqlQuery = "INSERT INTO directors (name) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sqlQuery, new String[]{"director_id"});
            statement.setString(1, director.getName());
            return statement;
        }, keyHolder);
        director.setId(keyHolder.getKey().longValue());
        return director;
    }

    @Override
    public Director update(Director director) {
        Integer count = jdbcTemplate.queryForObject("SELECT count(DIRECTOR_ID) FROM DIRECTORS WHERE DIRECTOR_ID = ?",
                Integer.class,
                director.getId());

        if (count == null || count == 0) {
            throw new DirectorNotFoundException("Director with id='" + director.getId() + "' not found");
        }
        String sqlQuery = "UPDATE directors SET name = ? WHERE director_id = ?";
        jdbcTemplate.update(sqlQuery, director.getName(), director.getId());
        return director;
    }

    @Override
    public List<Director> getAll() {
        String sqlQuery = "SELECT * FROM directors";
        return jdbcTemplate.query(sqlQuery, this::mapRowToDirector);
    }

    public Director mapRowToDirector(ResultSet resultSet, long i) throws SQLException {
        return Director.builder()
                .id(resultSet.getLong("director_id"))
                .name(resultSet.getString("name"))
                .build();
    }

    @Override
    public Director getById(Long id) {
        String sql = "select director_id, name from directors where director_id = ?";
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql, id);
        if (sqlRowSet.next()) {
            Director director = Director.builder()
                    .id(sqlRowSet.getLong("director_id"))
                    .name(sqlRowSet.getString("name"))
                    .build();
            log.info("Найден режиссер с id: {}, по имени {} ", sqlRowSet.getLong("director_id"),
                    sqlRowSet.getString("name"));
            return director;
        } else {
            log.info("Режиссер с id {} не найден", id);
            throw new DirectorNotFoundException("Режиссёр не найден.");
        }
    }

    @Override
    public Director remove(Long id) {
        Director director = getById(id);
        jdbcTemplate.update("DELETE FROM film_director WHERE director_id = ?", id);
        jdbcTemplate.update("DELETE FROM directors WHERE DIRECTOR_ID = ?", id);
        log.info(String.format("%s %d %s", "Режиссер с id ", id, " успешно удален"));
        return director;
    }
}
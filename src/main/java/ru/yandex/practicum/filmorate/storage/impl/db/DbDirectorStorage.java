package ru.yandex.practicum.filmorate.storage.impl.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DirectorAlreadyExistsException;
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
        String sqlSelectQuery = "SELECT COUNT(*) FROM directors WHERE name = ?";
        int count = jdbcTemplate.queryForObject(sqlSelectQuery, Integer.class, director.getName());
        if (count == 0) {
            String sqlInsertQuery = "INSERT INTO directors (name) VALUES (?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement statement = connection.prepareStatement(sqlInsertQuery, new String[]{"director_id"});
                statement.setString(1, director.getName());
                return statement;
            }, keyHolder);
            director.setId(keyHolder.getKey().longValue());
        } else {
            log.info("Режиссер с id {} уже есть в базе", director.getId());
            throw new DirectorAlreadyExistsException("Режиссер с id='" + director.getId() + " уже есть в базе");
        }
        return director;
    }

    @Override
    public Director update(Director director) {
        getById(director.getId());
        String sqlQuery = "UPDATE directors SET name = ? WHERE director_id = ?";
        jdbcTemplate.update(sqlQuery, director.getName(), director.getId());
        return director;
    }

    @Override
    public List<Director> getAll() {
        String sqlQuery = "SELECT * FROM directors";
        return jdbcTemplate.query(sqlQuery, this::mapRowToDirector);
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
        jdbcTemplate.update("DELETE FROM directors WHERE DIRECTOR_ID = ?", id);
        jdbcTemplate.update("DELETE FROM film_director WHERE director_id = ?", id);
        log.info(String.format("%s %d %s", "Режиссер с id ", id, " успешно удален"));
        return director;
    }

    private Director mapRowToDirector(ResultSet resultSet, long i) throws SQLException {
        return Director.builder()
                .id(resultSet.getLong("director_id"))
                .name(resultSet.getString("name"))
                .build();
    }
}
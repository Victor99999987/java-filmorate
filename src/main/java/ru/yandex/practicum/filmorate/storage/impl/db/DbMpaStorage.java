package ru.yandex.practicum.filmorate.storage.impl.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.MpaNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationMpaException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Qualifier("DbMpaStorage")
public class DbMpaStorage implements Storage<Mpa> {

    private final JdbcTemplate jdbcTemplate;

    public DbMpaStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Mpa> getAll() {
        List<Mpa> result = new ArrayList<>();
        String sql = "select * from mpa";
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql);
        while (sqlRowSet.next()) {
            result.add(makeMpa(sqlRowSet));
        }
        return result;
    }

    private Mpa makeMpa(SqlRowSet rs) {
        return Mpa.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .build();
    }

    @Override
    public Mpa getById(Long id) {
        String sql = "select * from mpa where id = ?";
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql, id);
        if (!sqlRowSet.first()) {
            log.info(String.format("MpaNotFoundException: Не найден mpa-рейтинг с id=%d", id));
            throw new MpaNotFoundException(String.format("Не найден mpa-рейтинг с id=%d", id));
        }
        return makeMpa(sqlRowSet);
    }

    @Override
    public Mpa add(Mpa mpa) {
        if (mpa.getId() != null) {
            log.info("ValidationMpaException: При добавлении mpa-рейтинг id должен быть null");
            throw new ValidationMpaException("При добавлении mpa-рейтинг id должен быть null");
        }
        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
        String sql = "insert into mpa (name) values(?)";
        jdbcTemplate.update(conn -> {
            PreparedStatement preparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, mpa.getName());
            return preparedStatement;
        }, generatedKeyHolder);
        mpa.setId(generatedKeyHolder.getKey().longValue());
        return mpa;
    }

    @Override
    public Mpa remove(Long id) {
        Mpa mpa = getById(id);
        String sql = "delete from mpa where id = ?";
        jdbcTemplate.update(sql, id);
        return mpa;
    }

    @Override
    public Mpa update(Mpa mpa) {
        getById(mpa.getId());
        String sql = "update mpa set name = ? where id = ?";
        jdbcTemplate.update(sql, mpa.getName(), mpa.getId());
        return mpa;
    }
}

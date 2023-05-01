package ru.yandex.practicum.filmorate.storage.impl.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationUserException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Qualifier("DbUserStorage")
public class DbUserStorage implements Storage<User> {

    private final JdbcTemplate jdbcTemplate;

    public DbUserStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<User> getAll() {
        List<User> result = new ArrayList<>();
        String sql = "select * from users";
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql);
        while (sqlRowSet.next()) {
            result.add(makeUser(sqlRowSet));
        }
        return result;
    }

    private User makeUser(SqlRowSet rs) {
        User user = User.builder()
                .id(rs.getLong("id"))
                .login(rs.getString("login"))
                .email(rs.getString("email"))
                .name(rs.getString("name"))
                .birthday(rs.getDate("birthday").toLocalDate())
                .build();

        String sql = "select friendship.friends_id from friendship where users_id = ?";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, rs.getLong("id"));
        while (rowSet.next()) {
            user.getFriends().add(rowSet.getLong("friends_id"));
        }

        return user;
    }

    @Override
    public User getById(Long id) {
        String sql = "select * from users where id = ?";
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql, id);
        if (!sqlRowSet.first()) {
            log.info(String.format("UserNotFoundException: Не найден пользователь с id=%d", id));
            throw new UserNotFoundException(String.format("Не найден пользователь с id=%d", id));
        }
        return makeUser(sqlRowSet);
    }

    @Override
    public User add(User user) {
        if (user.getId() != null) {
            log.info("ValidationUserException: При добавлении фильма id должен быть null");
            throw new ValidationUserException("При добавлении фильма id должен быть null");
        }
        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
        String sql = "insert into users (login, email, name, birthday) values(?, ?, ?, ?)";
        String finalSql = sql;
        jdbcTemplate.update(conn -> {
            PreparedStatement preparedStatement = conn.prepareStatement(finalSql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, user.getLogin());
            preparedStatement.setString(2, user.getEmail());
            preparedStatement.setString(3, user.getName());
            preparedStatement.setDate(4, Date.valueOf(user.getBirthday()));
            return preparedStatement;
        }, generatedKeyHolder);
        user.setId(generatedKeyHolder.getKey().longValue());
        for (Long friend : user.getFriends()) {
            sql = "insert into friendship (users_id, friends_id, confirmed) values(?, ?, ?)";
            jdbcTemplate.update(sql, user.getId(), friend, false);
        }
        return user;
    }

    @Override
    public User remove(Long id) {
        User user = getById(id);
        String sql = "delete from users where id = ?";
        jdbcTemplate.update(sql, id);
        return user;
    }

    @Override
    public User update(User user) {
        getById(user.getId());
        String sql = "update users set login = ?, email = ?, name = ?, birthday = ? where id = ?";
        jdbcTemplate.update(sql, user.getLogin(), user.getEmail(), user.getName(), Date.valueOf(user.getBirthday()),
                user.getId());
        sql = "delete from friendship where users_id = ?";
        jdbcTemplate.update(sql, user.getId());
        for (Long friend : user.getFriends()) {
            sql = "insert into friendship (users_id, friends_id, confirmed) values(?, ?, ?)";
            jdbcTemplate.update(sql, user.getId(), friend, false);
            sql = "update friendship set confirmed = ? where users_id = ? and friends_id = ?";
            jdbcTemplate.update(sql, true, friend, user.getId());
        }
        return user;
    }
}

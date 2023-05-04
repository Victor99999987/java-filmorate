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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Qualifier("DbUserStorage")
public class DbUserStorage extends DbStorage implements Storage<User> {

    public DbUserStorage(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public List<User> getAll() {
        String sql = "select u.id, u.login, u.email, u.name, u.birthday, f.friends_id from users as u\n" +
                "left join friendship as f on u.id = f.users_id";
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql);
        return makeUsers(sqlRowSet);
    }

    @Override
    public User getById(Long id) {
        String sql = "select u.id, u.login, u.email, u.name, u.birthday, f.friends_id from users as u\n" +
                "left join friendship as f on u.id = f.users_id where u.id = ?";
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql, id);
        if (!sqlRowSet.first()) {
            log.info(String.format("UserNotFoundException: Не найден пользователь с id=%d", id));
            throw new UserNotFoundException(String.format("Не найден пользователь с id=%d", id));
        }
        sqlRowSet.beforeFirst();
        return makeUsers(sqlRowSet).get(0);
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

        sql = "insert into friendship (users_id, friends_id, confirmed) values(?, ?, ?)";
        jdbcTemplate.batchUpdate(sql, user.getFriends(), user.getFriends().size(),
                (ps, friendsId) -> {
                    ps.setLong(1, user.getId());
                    ps.setLong(2, friendsId);
                    ps.setBoolean(3, false);
                });

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

        sql = "insert into friendship (users_id, friends_id, confirmed) values(?, ?, ?)";
        jdbcTemplate.batchUpdate(sql, user.getFriends(), user.getFriends().size(),
                (ps, friendsId) -> {
                    ps.setLong(1, user.getId());
                    ps.setLong(2, friendsId);
                    ps.setBoolean(3, false);
                });

        sql = "update friendship set confirmed = ? where users_id = ? and friends_id = ?";
        jdbcTemplate.batchUpdate(sql, user.getFriends(), user.getFriends().size(),
                (ps, friendsId) -> {
                    ps.setBoolean(1, true);
                    ps.setLong(2, friendsId);
                    ps.setLong(3, user.getId());
                });

        return user;
    }

    private List<User> makeUsers(SqlRowSet rs) {
        Map<Long, User> users = new HashMap<>();
        while (rs.next()) {
            Long id = rs.getLong("id");

            if (!users.containsKey(id)) {
                User user = User.builder()
                        .id(rs.getLong("id"))
                        .login(rs.getString("login"))
                        .email(rs.getString("email"))
                        .name(rs.getString("name"))
                        .birthday(rs.getDate("birthday").toLocalDate())
                        .build();
                users.put(id, user);
            }

            Long friendsId = rs.getLong("friends_id");
            if (!rs.wasNull()) {
                users.get(id).getFriends().add(friendsId);
            }
        }
        return new ArrayList<>(users.values());
    }

}

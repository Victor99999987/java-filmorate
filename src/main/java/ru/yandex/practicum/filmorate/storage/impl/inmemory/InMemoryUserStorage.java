package ru.yandex.practicum.filmorate.storage.impl.inmemory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.UserAlreadyExistException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationUserException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Qualifier("InMemoryUserStorage")
public class InMemoryUserStorage implements Storage<User> {
    private final Map<Long, User> users = new HashMap<>();
    private final Map<String, Long> userLogins = new HashMap<>();
    private Long generateId = 0L;

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User getById(Long id) {
        if (!users.containsKey(id)) {
            log.info(String.format("UserNotFoundException: Не найден пользователь с id=%d", id));
            throw new UserNotFoundException(String.format("Не найден пользователь с id=%d", id));
        }
        return users.get(id);
    }

    @Override
    public User add(User user) {
        if (user.getId() != null) {
            log.info("ValidationUserException: При добавлении пользователя id должен быть null");
            throw new ValidationUserException("При добавлении пользователя id должен быть null");
        }
        if (userLogins.containsKey(user.getLogin())) {
            log.info(String.format("UserAlreadyExistException: Пользователь с логином=%s уже существует", user.getLogin()));
            throw new UserAlreadyExistException(String.format("Пользователь с логином=%s уже существует", user.getLogin()));
        }
        user.setId(++generateId);
        users.put(user.getId(), user);
        userLogins.put(user.getLogin(), user.getId());
        return user;
    }

    @Override
    public User remove(Long id) {
        if (!users.containsKey(id)) {
            log.info(String.format("UserNotFoundException: При удалении не найден пользователь с id=%d", id));
            throw new UserNotFoundException(String.format("При удалении не найден пользователь с id=%d", id));
        }
        User user = users.get(id);
        users.remove(id);
        userLogins.remove(user.getLogin());
        return user;
    }

    @Override
    public User update(User user) {
        if (!users.containsKey(user.getId())) {
            log.info(String.format("UserNotFoundException: При обновлении не найден пользователь с id=%d", user.getId()));
            throw new UserNotFoundException(String.format("При обновлении не найден пользователь с id=%d", user.getId()));
        }
        User oldUser = users.get(user.getId());
        if (!oldUser.getLogin().equals(user.getLogin()) && userLogins.containsKey(user.getLogin())) {
            log.info(String.format("UserAlreadyExistException: Пользователь с логином=%s уже существует", user.getLogin()));
            throw new UserAlreadyExistException(String.format("Пользователь с логином=%s уже существует", user.getLogin()));
        }
        users.put(user.getId(), user);
        userLogins.remove(oldUser.getLogin());
        userLogins.put(user.getLogin(), user.getId());
        return user;
    }
}
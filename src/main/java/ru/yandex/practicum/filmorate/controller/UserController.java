package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationUserException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Set<User> users = new HashSet<>();
    private int generateId = 0;

    private int getGenerateId() {
        return ++generateId;
    }

    @GetMapping
    public List<User> findAll() {
        return new ArrayList<>(users);
    }

    @PostMapping
    public User create(@NotNull @Valid @RequestBody User user) {
        validationUser(user);
        user.setId(getGenerateId());
        if (users.contains(user)) {
            log.info("ValidationUserException: Такой пользователь уже есть");
            throw new ValidationUserException("Такой пользователь уже есть");
        }
        users.add(user);
        log.info("Добавлен пользователь {}", user.getLogin());
        return user;
    }

    @PutMapping
    public User update(@NotNull @Valid @RequestBody User user) {
        validationUser(user);
        if (!users.contains(user)) {
            log.info("UserNotFoundException: Нет такого пользователя в базе");
            throw new UserNotFoundException("Нет такого пользователя в базе");
        }
        log.info("Обновлен пользователь {}", user.getLogin());
        users.remove(user);
        users.add(user);
        return user;
    }

    private void validationUser(User user) {
        String name = user.getName();
        if (name == null || name.isEmpty() || name.isBlank()) {
            user.setName(user.getLogin());
        }
    }
}

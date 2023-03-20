package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationUserException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/users")
public class UserController {
    private final Set<User> users = new HashSet<>();
    private int generateId = 0;
    private final static Logger log = LoggerFactory.getLogger(UserController.class);

    private int getGenerateId() {
        return ++generateId;
    }

    @GetMapping
    public List<User> findAll() {
        return new ArrayList<>(users);
    }

    @PostMapping
    public User create(@RequestBody User user) {
        validationUser(user);
        user.setId(getGenerateId());
        if(users.contains(user)) {
            log.info("ValidationUserException: Такой пользователь уже есть");
            throw new ValidationUserException("Такой пользователь уже есть");
        }
        users.add(user);
        log.info("Добавлен пользователь {}", user.getLogin());
        return user;
    }

    @PutMapping
    public User update(@RequestBody User user) {
        validationUser(user);
        if(!users.contains(user)) {
            log.info("UserNotFoundException: Нет такого пользователя в базе");
            throw new UserNotFoundException("Нет такого пользователя в базе");
        }
        log.info("Обновлен пользователь {}", user.getLogin());
        users.remove(user);
        users.add(user);
        return user;
    }

    private void validationUser(User user) {
        if(user == null) {
            log.info("ValidationUserException: Пустой запрос");
            throw new ValidationUserException("Пустой запрос");
        }
        String email = user.getEmail();
        if(email==null || email.isEmpty() || email.contains(" ") || !email.contains("@")) {
            log.info("ValidationUserException: Неверный email");
            throw new ValidationUserException("Неверный email");
        }
        String login = user.getLogin();
        if(login == null || login.isEmpty() || login.contains(" ")) {
            log.info("ValidationUserException: Логин не может быть пустым и содержать пробелы");
            throw new ValidationUserException("Логин не может быть пустым и содержать пробелы");
        }
        String name = user.getName();
        if(name == null || name.isEmpty() || name.isBlank()){
            user.setName(user.getLogin());
        }
        LocalDate birthday = user.getBirthday();
        if(birthday.isAfter(LocalDate.now())){
            log.info("ValidationUserException: Дата рождения не может быть в будущем");
            throw new ValidationUserException("Дата рождения не может быть в будущем");
        }
    }
}

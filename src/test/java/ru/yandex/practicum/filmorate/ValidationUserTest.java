package ru.yandex.practicum.filmorate;


import com.google.gson.Gson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationUserException;
import ru.yandex.practicum.filmorate.model.User;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;

@SpringBootTest
public class ValidationUserTest {

    private User user;
    private UserController userController = new UserController();

    @BeforeEach
    public void init(){
        user = new User(1, "login", "email@mail.ru", "name",
                        LocalDate.of(1900, 12, 1));
    }

    @Test
    public void mailIsEmpty() {
        user.setEmail("");
        ValidationUserException e = Assertions.assertThrows(
                ValidationUserException.class,
                () -> userController.create(user));
        Assertions.assertEquals("Неверный email", e.getMessage());
    }

    @Test
    public void emailIsBlank() {
        user.setEmail("   ");
        ValidationUserException e = Assertions.assertThrows(
                ValidationUserException.class,
                () -> userController.create(user));
        Assertions.assertEquals("Неверный email", e.getMessage());
    }

    @Test
    public void emailIsNull() {
        user.setEmail(null);
        ValidationUserException e = Assertions.assertThrows(
                ValidationUserException.class,
                () -> userController.create(user));
        Assertions.assertEquals("Неверный email", e.getMessage());
    }

    @Test
    public void emailIsIncorrect() {
        user.setEmail("emailDOGmail.ru");
        ValidationUserException e = Assertions.assertThrows(
                ValidationUserException.class,
                () -> userController.create(user));
        Assertions.assertEquals("Неверный email", e.getMessage());
    }

    @Test
    public void loginIsEmpty() {
        user.setLogin("");
        ValidationUserException e = Assertions.assertThrows(
                ValidationUserException.class,
                () -> userController.create(user));
        Assertions.assertEquals("Логин не может быть пустым и содержать пробелы", e.getMessage());
    }

    @Test
    public void loginIsNull() {
        user.setLogin(null);
        ValidationUserException e = Assertions.assertThrows(
                ValidationUserException.class,
                () -> userController.create(user));
        Assertions.assertEquals("Логин не может быть пустым и содержать пробелы", e.getMessage());
    }

    @Test
    public void loginWithSpace() {
        user.setLogin("login loginovi4");
        ValidationUserException e = Assertions.assertThrows(
                ValidationUserException.class,
                () -> userController.create(user));
        Assertions.assertEquals("Логин не может быть пустым и содержать пробелы", e.getMessage());
    }

    @Test
    public void nameIsNull() {
        user.setName(null);
        User user2 = userController.create(user);
        Assertions.assertEquals(user.getLogin(), user2.getName());
        Assertions.assertEquals(1, userController.findAll().size());
    }

    @Test
    public void dateIsTomorrow() {
        user.setBirthday(LocalDate.now().plusDays(1));
        ValidationUserException e = Assertions.assertThrows(
                ValidationUserException.class,
                () -> userController.create(user));
        Assertions.assertEquals("Дата рождения не может быть в будущем", e.getMessage());
    }

    @Test
    public void unknownUser() {
        user.setId(9999);
        UserNotFoundException e = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> userController.update(user));
        Assertions.assertEquals("Нет такого пользователя в базе", e.getMessage());
    }

    @Test
    public void normalCreateAndUpdate() {
        user = userController.create(user);
        user.setName("updateName");
        user.setEmail("update@email.ru");
        User user2 = userController.update(user);
        Assertions.assertEquals(user.getName(), user2.getName());
        Assertions.assertEquals(user.getEmail(), user2.getEmail());
        Assertions.assertEquals(1, userController.findAll().size());
    }

}

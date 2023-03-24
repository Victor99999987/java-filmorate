package ru.yandex.practicum.filmorate;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

@SpringBootTest
public class ValidationUserTest {

    @Autowired
    private UserController userController;
    private User user;

    @BeforeEach
    public void init(){
        user = new User(1, "login", "email@mail.ru", "name",
                        LocalDate.of(1900, 12, 1));
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

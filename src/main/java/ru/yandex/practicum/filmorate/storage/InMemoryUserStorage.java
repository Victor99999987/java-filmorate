package ru.yandex.practicum.filmorate.controller;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

@Component
public class InMemoryUserStorage implements UserStorage {
    @Override
    public User add(User user) {
        return null;
    }

    @Override
    public User remove(User user) {
        return null;
    }

    @Override
    public User update(User user) {
        return null;
    }
}

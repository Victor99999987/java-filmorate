package ru.yandex.practicum.filmorate.controller;

import ru.yandex.practicum.filmorate.model.User;

public interface UserStorage {
    public User add(User user);
    public User remove(User user);
    public User update(User user);
}

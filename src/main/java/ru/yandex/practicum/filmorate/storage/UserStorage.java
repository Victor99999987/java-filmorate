package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    public List<User> getAll();

    public User getById(Long id);

    public User add(User user);

    public User remove(Long id);

    public User update(User user);
}

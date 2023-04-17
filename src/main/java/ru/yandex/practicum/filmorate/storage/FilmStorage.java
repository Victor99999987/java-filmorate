package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    public List<Film> getAll();

    public Film getById(Long id);

    public Film add(Film film);

    public Film remove(Long id);

    public Film update(Film film);
}

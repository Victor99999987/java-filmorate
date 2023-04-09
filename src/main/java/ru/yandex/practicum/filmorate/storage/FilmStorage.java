package ru.yandex.practicum.filmorate.controller;

import ru.yandex.practicum.filmorate.model.Film;

public interface FilmStorage {
    public Film add(Film Film);
    public Film remove(Film Film);
    public Film update(Film Film);
}

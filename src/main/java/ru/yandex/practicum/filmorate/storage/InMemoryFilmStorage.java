package ru.yandex.practicum.filmorate.controller;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    @Override
    public Film add(Film Film) {
        return null;
    }

    @Override
    public Film remove(Film Film) {
        return null;
    }

    @Override
    public Film update(Film Film) {
        return null;
    }
}

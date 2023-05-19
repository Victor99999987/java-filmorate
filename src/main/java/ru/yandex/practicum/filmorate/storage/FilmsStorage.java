package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmsStorage extends Storage<Film> {
    List<Film> getFilmsThatUserLikes(long userId);
}

package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.type.RequestType;

import java.util.List;

public interface FilmStorage extends Storage<Film> {
    List<Film> findFilmsSortByLikesAndYear(Long directorId, RequestType requestType);

    List<Film> searchFilms(String query, String by);
}

package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserAlreadyExistException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationFilmException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final Storage<Film> filmStorage;
    private final Storage<User> userStorage;

    public FilmService(@Qualifier("DbFilmStorage") Storage<Film> filmStorage,
                       @Qualifier("DbUserStorage") Storage<User> userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public List<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film getById(Long id) {
        return filmStorage.getById(id);
    }

    public Film add(Film film) {
        validationFilm(film);
        return filmStorage.add(film);
    }

    private void validationFilm(Film film) {
        LocalDate releaseDate = film.getReleaseDate();
        if (releaseDate.isBefore(LocalDate.of(1895, 12, 28))) {
            log.info("ValidationFilmException: дата релиза — не раньше 28 декабря 1895 года");
            throw new ValidationFilmException("дата релиза — не раньше 28 декабря 1895 года");
        }
    }

    public Film remove(Long id) {
        return filmStorage.remove(id);
    }

    public Film update(Film film) {
        validationFilm(film);
        return filmStorage.update(film);
    }

    public Film addLike(Long id, Long userId) {
        Film film = filmStorage.getById(id);
        userStorage.getById(userId);
        if (film.getLikes().contains(userId)) {
            log.info(String.format("UserAlreadyExistException: Фильм с id=%d уже лайкнут пользователем с id=%s", id, userId));
            throw new UserAlreadyExistException(String.format("Фильм с id=%d уже лайкнут пользователем с id=%s", id, userId));
        }
        film.getLikes().add(userId);
        filmStorage.update(film);
        return film;
    }

    public Film removeLike(Long id, Long userId) {
        Film film = filmStorage.getById(id);
        userStorage.getById(userId);
        if (!film.getLikes().contains(userId)) {
            log.info(String.format("UserNotFoundException: Фильм с id=%d не лайкал пользователь с id=%s", id, userId));
            throw new UserNotFoundException(String.format("Фильм с id=%d не лайкал пользователь с id=%s", id, userId));
        }
        film.getLikes().remove(userId);
        filmStorage.update(film);
        return film;
    }

    public List<Film> getPopularFilms(Long count) {
        return filmStorage.getAll().stream()
                .sorted(Comparator.comparingLong(film -> -1 * film.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }
}

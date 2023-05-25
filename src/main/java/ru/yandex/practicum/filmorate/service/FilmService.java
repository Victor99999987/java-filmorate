package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserAlreadyExistException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationFilmException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.type.EventType;
import ru.yandex.practicum.filmorate.model.type.OperationType;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class FilmService {
    private final Storage<User> userStorage;
    private final Storage<Genre> genreStorage;
    private final Storage<Event> eventStorage;
    private final FilmStorage filmStorage;

    public FilmService(@Qualifier("DbFilmStorage") FilmStorage filmStorage,
                       @Qualifier("DbUserStorage") Storage<User> userStorage,
                       @Qualifier("DbGenreStorage") Storage<Genre> genreStorage,
                       @Qualifier("DbEventStorage") Storage<Event> eventStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
        this.eventStorage = eventStorage;
    }

    public List<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film getById(Long id) {
        return filmStorage.getById(id);
    }

    public Film add(Film film) {
        return filmStorage.add(film);
    }

    public Film remove(Long id) {
        return filmStorage.remove(id);
    }

    public Film update(Film film) {
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
        Event event = Event.builder()
                .timestamp(Instant.now().toEpochMilli())
                .userId(userId)
                .operation(OperationType.ADD)
                .eventType(EventType.LIKE)
                .entityId(id)
                .build();
        eventStorage.add(event);
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
        Event event = Event.builder()
                .timestamp(Instant.now().toEpochMilli())
                .userId(userId)
                .operation(OperationType.REMOVE)
                .eventType(EventType.LIKE)
                .entityId(id)
                .build();
        eventStorage.add(event);
        return film;
    }

    public List<Film> getPopularFilms(Long count, Long genreId, Long year) {
        Stream<Film> films = filmStorage.getAll().stream();
        if (genreId != null) {
            films = films.filter(film -> film.getGenres().contains(genreStorage.getById(genreId)));
        }
        if (year != null) {
            if (year < 1895) {
                throw new ValidationFilmException("Первый фильм был снят в 1895 году, " +
                        "проверьте параметры запроса.");
            }
            films = films.filter(film -> film.getReleaseDate().getYear() == year);
        }
        return films.sorted(Comparator.comparingLong(film -> -1 * film.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    public List<Film> getCommonFilms(long userId, long friendId) {
        userStorage.getById(userId);
        userStorage.getById(friendId);
        log.info("Получили список общих фильмов пользователей id = {} и id = {}", userId, friendId);
        return filmStorage.getAll().stream()
                .filter(film -> film.getLikes().contains(userId) && film.getLikes().contains(friendId))
                .sorted(Comparator.comparingLong(film -> -1 * film.getLikes().size()))
                .collect(Collectors.toList());
    }

    public List<Film> getFilmsSortByLikesAndYear(Long directorId, String param) {
        return filmStorage.findFilmsSortByLikesAndYear(directorId, param);
    }
}

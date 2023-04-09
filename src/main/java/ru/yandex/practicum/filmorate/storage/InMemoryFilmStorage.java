package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationFilmException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private Long generateId = 0L;

    @Override
    public List<Film> getAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film getById(Long id) {
        if (!films.containsKey(id)) {
            log.info(String.format("FilmNotFoundException: Не найден фильм с id=%d", id));
            throw new FilmNotFoundException(String.format("Не найден фильм с id=%d", id));
        }
        return films.get(id);
    }

    @Override
    public Film add(Film film) {
        if (film.getId() != null) {
            log.info("ValidationFilmException: При добавлении фильма id должен быть null");
            throw new ValidationFilmException("При добавлении фильма id должен быть null");
        }
        film.setId(++generateId);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film remove(Long id) {
        if (!films.containsKey(id)) {
            log.info(String.format("FilmNotFoundException: При удалении не найден фильм с id=%d", id));
            throw new FilmNotFoundException(String.format("При удалении не найден фильм с id=%d", id));
        }
        Film film = films.get(id);
        films.remove(id);
        return film;
    }

    @Override
    public Film update(Film film) {
        if (!films.containsKey(film.getId())) {
            log.info(String.format("FilmNotFoundException: При обновлении не найден фильм с id=%d", film.getId()));
            throw new FilmNotFoundException(String.format("При обновлении не найден фильм с id=%d", film.getId()));
        }
        films.put(film.getId(), film);
        return film;
    }
}

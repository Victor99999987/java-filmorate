package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationFilmException;
import ru.yandex.practicum.filmorate.exception.ValidationUserException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/films")
public class FilmController {
    private final Set<Film> films = new HashSet<>();
    private int generateId = 0;
    private final static Logger log = LoggerFactory.getLogger(FilmController.class);

    private int getGenerateId() {
        return ++generateId;
    }

    @GetMapping
    public List<Film> findAll() {
        return new ArrayList<>(films);
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        validationFilm(film);
        film.setId(getGenerateId());
        if(films.contains(film)) {
            log.info("ValidationFilmException: Такой фильм уже есть");
            throw new ValidationFilmException("Такой фильм уже есть");
        }
        films.add(film);
        log.info("Добавлен фильм {}", film.getName());
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        validationFilm(film);
        if(!films.contains(film)) {
            log.info("FilmNotFoundException: Нет такого фильма в базе");
            throw new FilmNotFoundException("Нет такого фильма в базе");
        }
        log.info("Обновлен фильм {}", film.getName());
        films.remove(film);
        films.add(film);
        return film;
    }

    private void validationFilm(Film film) {
        if(film == null) {
            log.info("ValidationFilmException: Пустой запрос");
            throw new ValidationFilmException("Пустой запрос");
        }
        String name = film.getName();
        if (name == null || name.isEmpty() || name.isBlank()) {
            log.info("ValidationFilmException: Название не может быть пустым");
            throw new ValidationFilmException("Название не может быть пустым");
        }
        String description = film.getDescription();
        if (description.length() > 200) {
            log.info("ValidationFilmException: Максимальная длина описания — 200 символов");
            throw new ValidationFilmException("Максимальная длина описания — 200 символов");
        }
        LocalDate releaseDate = film.getReleaseDate();
        if (releaseDate.isBefore(LocalDate.of(1895, 12, 28))) {
            log.info("ValidationFilmException: дата релиза — не раньше 28 декабря 1895 года");
            throw new ValidationFilmException("дата релиза — не раньше 28 декабря 1895 года");
        }
        int duration = film.getDuration();
        if (duration < 0) {
            log.info("ValidationFilmException: продолжительность фильма должна быть положительной");
            throw new ValidationFilmException("продолжительность фильма должна быть положительной");
        }
    }

}

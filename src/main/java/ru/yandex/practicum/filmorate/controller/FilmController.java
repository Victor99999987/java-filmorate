package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationFilmException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Set<Film> films = new HashSet<>();
    private int generateId = 0;

    private int getGenerateId() {
        return ++generateId;
    }

    @GetMapping
    public List<Film> findAll() {
        return new ArrayList<>(films);
    }

    @PostMapping
    public Film create(@NotNull @Valid @RequestBody Film film) {
        validationFilm(film);
        film.setId(getGenerateId());
        if (films.contains(film)) {
            log.info("ValidationFilmException: Такой фильм уже есть");
            throw new ValidationFilmException("Такой фильм уже есть");
        }
        films.add(film);
        log.info("Добавлен фильм {}", film.getName());
        return film;
    }

    @PutMapping
    public Film update(@NotNull @Valid @RequestBody Film film) {
        validationFilm(film);
        if (!films.contains(film)) {
            log.info("FilmNotFoundException: Нет такого фильма в базе");
            throw new FilmNotFoundException("Нет такого фильма в базе");
        }
        log.info("Обновлен фильм {}", film.getName());
        films.remove(film);
        films.add(film);
        return film;
    }

    private void validationFilm(Film film) {
        LocalDate releaseDate = film.getReleaseDate();
        if (releaseDate.isBefore(LocalDate.of(1895, 12, 28))) {
            log.info("ValidationFilmException: дата релиза — не раньше 28 декабря 1895 года");
            throw new ValidationFilmException("дата релиза — не раньше 28 декабря 1895 года");
        }
    }

}

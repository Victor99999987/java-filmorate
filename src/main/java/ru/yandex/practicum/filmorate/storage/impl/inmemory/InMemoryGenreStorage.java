package ru.yandex.practicum.filmorate.storage.impl.inmemory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.GenreNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationGenreException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Qualifier("InMemoryGenreStorage")
public class InMemoryGenreStorage implements Storage<Genre> {

    private final Map<Long, Genre> genres = new HashMap<>();
    private Long generateId = 0L;

    @Override
    public List<Genre> getAll() {
        return new ArrayList<>(genres.values());
    }

    @Override
    public Genre getById(Long id) {
        if (!genres.containsKey(id)) {
            log.info(String.format("GenreNotFoundException: Не найден жанр с id=%d", id));
            throw new GenreNotFoundException(String.format("Не найден жанр с id=%d", id));
        }
        return genres.get(id);
    }

    @Override
    public Genre add(Genre genre) {
        if (genre.getId() != null) {
            log.info("ValidationGenreException: При добавлении жанра id должен быть null");
            throw new ValidationGenreException("При добавлении жанра id должен быть null");
        }
        genre.setId(++generateId);
        genres.put(genre.getId(), genre);
        return genre;
    }

    @Override
    public Genre remove(Long id) {
        if (!genres.containsKey(id)) {
            log.info(String.format("GenreNotFoundException: При удалении не найден жанр с id=%d", id));
            throw new GenreNotFoundException(String.format("При удалении не найден жанр с id=%d", id));
        }
        Genre genre = genres.get(id);
        genres.remove(id);
        return genre;
    }

    @Override
    public Genre update(Genre genre) {
        if (!genres.containsKey(genre.getId())) {
            log.info(String.format("GenreNotFoundException: При обновлении не найден жанр с id=%d", genre.getId()));
            throw new GenreNotFoundException(String.format("При обновлении не найден жанр с id=%d", genre.getId()));
        }
        genres.put(genre.getId(), genre);
        return genre;
    }
}

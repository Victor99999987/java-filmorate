package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.util.List;

@Slf4j
@Service
public class GenreService {
    private final Storage<Genre> genreStorage;

    public GenreService(@Qualifier("DbGenreStorage") Storage<Genre> genreStorage) {
        this.genreStorage = genreStorage;
    }

    public List<Genre> getAll() {
        return genreStorage.getAll();
    }

    public Genre getById(Long id) {
        return genreStorage.getById(id);
    }

    public Genre add(Genre genre) {
        return genreStorage.add(genre);
    }

    public Genre remove(Long id) {
        return genreStorage.remove(id);
    }

    public Genre update(Genre genre) {
        return genreStorage.update(genre);
    }

}

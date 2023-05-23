package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DirectorNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.impl.db.DbDirectorStorage;

import java.util.Collection;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class DirectorService {
    private final DbDirectorStorage directorStorage;

    public Director add(Director director) {
        log.info("Добавляем режиссера в коллекцию");
        return directorStorage.add(director);
    }

    public Director getById(Long id) {
        Optional<Director> optionalDirector = Optional.ofNullable(directorStorage.getById(id));
        if (optionalDirector.isPresent()) {
            return optionalDirector.get();
        } else {
            throw new DirectorNotFoundException("Режиссер не найден!");
        }
    }

    public Director update(Director director) {
        return directorStorage.update(director);
    }

    public Collection<Director> getAll() {
        log.info("Выводим список всех режиссеров");
        return directorStorage.getAll();
    }

    public void remove(Long id) {
        log.info(String.format("Удаляем режиссера с id: %s", id));
        try {
            directorStorage.remove(id);
        } catch (DirectorNotFoundException e) {
            throw new DirectorNotFoundException("Режиссер с таким id отсутствует в базе");
        }
    }
}
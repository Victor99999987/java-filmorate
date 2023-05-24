package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.impl.db.DbDirectorStorage;

import java.util.Collection;

@Service
@Slf4j
public class DirectorService {
    private final DbDirectorStorage directorStorage;

    public DirectorService(@Qualifier("DbDirectorStorage") DbDirectorStorage directorStorage) {
        this.directorStorage = directorStorage;
    }

    public Director add(Director director) {
        log.info("Добавляем режиссера в коллекцию");
        return directorStorage.add(director);
    }

    public Director getById(Long id) {
        return directorStorage.getById(id);
    }

    public Director update(Director director) {
        log.info("Обновляем информацию о режиссёре");
        return directorStorage.update(director);
    }

    public Collection<Director> getAll() {
        log.info("Выводим список всех режиссеров");
        return directorStorage.getAll();
    }

    public void remove(Long id) {
        log.info(String.format("Удаляем режиссера с id: %s", id));
        directorStorage.remove(id);
    }
}
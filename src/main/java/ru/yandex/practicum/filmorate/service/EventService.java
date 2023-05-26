package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.EventStorage;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.util.List;

@Slf4j
@Service
public class EventService {
    private final EventStorage eventStorage;
    private final Storage<User> userStorage;

    public EventService(@Qualifier("DbEventStorage") EventStorage eventStorage,
                        @Qualifier("DbUserStorage") Storage<User> userStorage) {
        this.eventStorage = eventStorage;
        this.userStorage = userStorage;
    }

    public List<Event> getAll() {
        log.info("Запрошена лента всех событий");
        return eventStorage.getAll();
    }

    public List<Event> getFeedByUserId(Long id) {
        log.info("Запрошена лента событий для пользователя с id = {}.", id);
        userStorage.getById(id);
        return eventStorage.getFeedByUserId(id);
    }

}

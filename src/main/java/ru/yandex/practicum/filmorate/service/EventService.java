package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EventService {
    private final Storage<Event> eventStorage;
    private final Storage<User> userStorage;

    public EventService(@Qualifier("DbEventStorage") Storage<Event> eventStorage,
                        @Qualifier("DbUserStorage") Storage<User> userStorage) {
        this.eventStorage = eventStorage;
        this.userStorage = userStorage;
    }

    public List<Event> getAll() {
        return eventStorage.getAll();
    }

    public List<Event> getFeedByUserId(Long id) {
        userStorage.getById(id);
        return getAll().stream()
                .filter(event -> event.getUserId().equals(id))
                .collect(Collectors.toList());
    }

}

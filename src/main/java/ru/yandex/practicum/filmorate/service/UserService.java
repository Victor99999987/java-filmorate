package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserAlreadyExistException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.type.EventType;
import ru.yandex.practicum.filmorate.model.type.OperationType;
import ru.yandex.practicum.filmorate.storage.EventStorage;
import ru.yandex.practicum.filmorate.storage.Storage;
import ru.yandex.practicum.filmorate.storage.impl.db.DbFilmStorage;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final Storage<User> userStorage;
    private final EventStorage eventStorage;
    private final DbFilmStorage filmStorage;

    public UserService(@Qualifier("DbUserStorage") Storage<User> userStorage,
                       @Qualifier("DbEventStorage") EventStorage eventStorage,
                       @Qualifier("DbFilmStorage") DbFilmStorage filmStorage) {
        this.userStorage = userStorage;
        this.eventStorage = eventStorage;
        this.filmStorage = filmStorage;
    }

    public List<User> getAll() {
        return userStorage.getAll();
    }

    public User getById(Long id) {
        return userStorage.getById(id);
    }

    public User add(User user) {
        validationUser(user);
        return userStorage.add(user);
    }

    private void validationUser(User user) {
        String name = user.getName();
        if (name == null || name.isEmpty() || name.isBlank()) {
            user.setName(user.getLogin());
        }
    }

    public User remove(Long id) {
        return userStorage.remove(id);
    }

    public User update(User user) {
        validationUser(user);
        return userStorage.update(user);
    }

    public List<User> getFriends(Long id) {
        return userStorage.getById(id).getFriends().stream()
                .map(userStorage::getById)
                .collect(Collectors.toList());
    }

    public User addFriend(Long id, Long friendId) {
        User user = userStorage.getById(id);
        User friend = userStorage.getById(friendId);
        if (user.getFriends().contains(friendId)) {
            log.info(String.format("UserAlreadyExistException: У пользователя с id=%d уже есть друг с id=%s", id, friendId));
            throw new UserAlreadyExistException(String.format("У пользователя с id=%d уже есть друг с id=%s", id, friendId));
        }
        user.getFriends().add(friendId);
        userStorage.update(user);
        Event event = Event.builder()
                .timestamp(Instant.now().toEpochMilli())
                .userId(id)
                .operation(OperationType.ADD)
                .eventType(EventType.FRIEND)
                .entityId(friendId)
                .build();
        eventStorage.add(event);
        return user;
    }

    public User removeFriend(Long id, Long friendId) {
        User user = userStorage.getById(id);
        User friend = userStorage.getById(friendId);
        if (!user.getFriends().contains(friendId)) {
            log.info(String.format("UserNotFoundException: У пользователя с id=%d нет друга с id=%s", id, friendId));
            throw new UserNotFoundException(String.format("У пользователя с id=%d нет друга с id=%s", id, friendId));
        }
        user.getFriends().remove(friendId);
        userStorage.update(user);
        Event event = Event.builder()
                .timestamp(Instant.now().toEpochMilli())
                .userId(id)
                .operation(OperationType.REMOVE)
                .eventType(EventType.FRIEND)
                .entityId(friendId)
                .build();
        eventStorage.add(event);
        return user;
    }

    public List<User> getCommonFriends(Long id, Long otherId) {
        Set<Long> friends = new HashSet<>(userStorage.getById(id).getFriends());
        Set<Long> otherFriends = userStorage.getById(otherId).getFriends();
        friends.retainAll(otherFriends);
        return friends.stream()
                .map(userStorage::getById)
                .collect(Collectors.toList());
    }

    public List<Film> getMovieRecommendations(Long userId) {
        getById(userId);
        return filmStorage.getMovieRecommendations(userId);
    }

}
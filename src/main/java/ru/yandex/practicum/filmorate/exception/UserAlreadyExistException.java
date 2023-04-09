package ru.yandex.practicum.filmorate.exception;

public class UserAlreadyExsistException extends RuntimeException {
    public UserAlreadyExsistException(String message) {
        super(message);
    }
}

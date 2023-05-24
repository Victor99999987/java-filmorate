package ru.yandex.practicum.filmorate.exception;

public class ValidationEventException extends RuntimeException {
    public ValidationEventException(String message) {
        super(message);
    }
}

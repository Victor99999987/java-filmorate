package ru.yandex.practicum.filmorate.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.BAD_REQUEST)
public class ValidationUserException extends RuntimeException {

    public ValidationUserException(String message) {
        super(message);
    }

}

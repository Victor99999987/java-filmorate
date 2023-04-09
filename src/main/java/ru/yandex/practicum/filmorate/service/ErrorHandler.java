package ru.yandex.practicum.filmorate.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.ErrorResponse;

@RestControllerAdvice
public class ExceptionHandler {

//    @ExceptionHandler({IllegalArgumentException.class, NullPointerException.class})
//// в аргументах указывается родительское исключение
//    public Map<String, String> handleIncorrectCount(final RuntimeException e) {
//        return Map.of(
//                "error", "Ошибка с параметром count.",
//                "errorMessage", e.getMessage()
//        );
//    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handle(final FilmNotFoundException e) {
        return new ErrorResponse("Ошибка данных", e.getMessage());
    }

}

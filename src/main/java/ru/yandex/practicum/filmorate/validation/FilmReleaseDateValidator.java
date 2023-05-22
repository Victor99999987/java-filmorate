package ru.yandex.practicum.filmorate.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class FilmReleaseDateValidator implements ConstraintValidator<FilmReleaseDate, LocalDate> {

    @Override
    public boolean isValid(final LocalDate valueToValidate, final ConstraintValidatorContext context) {
        return valueToValidate != null
                && valueToValidate.isAfter(LocalDate.of(1895, 12, 28));
    }
}

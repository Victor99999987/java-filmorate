package ru.yandex.practicum.filmorate.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = FilmReleaseDateValidator.class)
@Target({TYPE, FIELD, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Documented
public @interface FilmReleaseDate {

    String message() default "{ru.yandex.practicum.filmorate.validation.FilmReleaseDate.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
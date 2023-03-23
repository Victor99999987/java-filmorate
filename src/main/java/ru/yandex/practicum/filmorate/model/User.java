package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Email;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class User {
    @EqualsAndHashCode.Include
    private Integer id;
    @Pattern(regexp = "^[A-Za-z0-9-.]+$", message = "Логин не может быть пустым и содержать пробелы")
    @EqualsAndHashCode.Exclude
    private String login;
    @Email(message = "Неверный Email")
    @EqualsAndHashCode.Exclude
    private String email;
    @EqualsAndHashCode.Exclude
    private String name;
    @Past(message = "дата рождения не может быть в будущем")
    @EqualsAndHashCode.Exclude
    private LocalDate birthday;
}

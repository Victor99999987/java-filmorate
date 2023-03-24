package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class Film {
    @EqualsAndHashCode.Include
    private Integer id;
    @NotBlank(message = "Название фильма не может быть пустым")
    @EqualsAndHashCode.Exclude
    private String name;
    @Size(max = 200, message = "Максимальная длина описания фильма — 200 символов")
    @EqualsAndHashCode.Exclude
    private String description;
    @EqualsAndHashCode.Exclude
    private LocalDate releaseDate;
    @Positive
    @EqualsAndHashCode.Exclude
    private int duration;
}

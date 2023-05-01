package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@Builder
public class Film {
    @EqualsAndHashCode.Include
    private Long id;
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
    @EqualsAndHashCode.Exclude
    private final Set<Long> likes = new HashSet<>();
    @EqualsAndHashCode.Exclude
    private final Set<Genre> genres = new HashSet<>();
    @EqualsAndHashCode.Exclude
    private Mpa mpa;
}

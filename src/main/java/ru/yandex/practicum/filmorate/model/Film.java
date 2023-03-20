package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class Film {
    @EqualsAndHashCode.Include
    private Integer id;
    @EqualsAndHashCode.Exclude
    private String name;
    @EqualsAndHashCode.Exclude
    private String description;
    @EqualsAndHashCode.Exclude
    private LocalDate releaseDate;
    @EqualsAndHashCode.Exclude
    private int duration;
}

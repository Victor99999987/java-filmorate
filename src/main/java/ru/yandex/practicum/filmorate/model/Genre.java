package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@Builder
public class Genre {
    @EqualsAndHashCode.Include
    private Long id;
    @NotBlank(message = "Название жанра не может быть пустым")
    @EqualsAndHashCode.Exclude
    private String name;
}

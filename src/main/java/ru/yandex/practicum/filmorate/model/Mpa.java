package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@Builder
public class Mpa {
    @EqualsAndHashCode.Include
    private Long id;
    @NotBlank(message = "Название mpa-рейтинга не может быть пустым")
    @EqualsAndHashCode.Exclude
    private String name;
}

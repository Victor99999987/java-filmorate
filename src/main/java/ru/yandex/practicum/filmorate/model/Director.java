package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@Builder
public class Director {

    @EqualsAndHashCode.Include
    private Long id;

    @EqualsAndHashCode.Exclude
    @NotBlank(message = "Имя режиссёра не должно быть пустым")
    private String name;
}
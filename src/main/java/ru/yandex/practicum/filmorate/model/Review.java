package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

@Data
@AllArgsConstructor
@Builder
public class Review {
    @PositiveOrZero
    private long reviewId;
    @NotNull
    private Long filmId;
    @NotNull
    private Long userId;
    @NotBlank(message = "Текст отзыва не может быть пустым.")
    private String content;
    @NotNull
    private Boolean isPositive;
    private long useful;

    public boolean getIsPositive() {
        return isPositive;
    }
}

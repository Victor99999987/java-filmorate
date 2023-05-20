package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    private long filmId;
    @NotNull
    private long userId;
    @NotNull(message = "У отзыва должен быть текст.")
    @NotBlank(message = "Текст отзыва не может быть пустым.")
    private String content;
    @NotNull
    private boolean isPositive;
    private long useful;

    @JsonCreator
    public Review(@JsonProperty("reviewId") Long reviewId,
                @JsonProperty("filmId") long filmId,
                @JsonProperty("userId") long userId,
                @JsonProperty("content") String content,
                @JsonProperty("isPositive") boolean isPositive,
                @JsonProperty("useful") long useful) {
        this.reviewId = (reviewId == null) ? 0 : reviewId;
        this.filmId = filmId;
        this.userId = userId;
        this.content = content;
        this.isPositive = isPositive;
        this.useful = useful;
    }
}

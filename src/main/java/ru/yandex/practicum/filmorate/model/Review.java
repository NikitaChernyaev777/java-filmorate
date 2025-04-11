package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class Review {
    private Long reviewId;

    @NotNull
    private Long filmId;

    @NotNull
    private Long userId;

    @NotBlank(message = "Текст отзыва не может быть пустым!")
    private String content;

    @NotNull
    private Boolean isPositive;

    private int useful;
}
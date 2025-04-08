package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.filmorate.validation.annotation.ReleaseDateConstraint;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Film {

    private Long id;

    @NotBlank(message = "Название фильма не может быть пустым!")
    private String name;

    @NotBlank(message = "Название фильма не может быть пустым!")
    @Size(max = 200, message = "Описание фильма не может занимать более 200 символов!")
    private String description;

    @NotNull
    @ReleaseDateConstraint
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительным числом!")
    private Integer duration;

    private MpaRating mpa;

    private Set<Long> likes = new HashSet<>();

    private Set<Genre> genres = new TreeSet<>(Comparator.comparing(Genre::getId));

    private Set<Director> directors = new HashSet<>();
}
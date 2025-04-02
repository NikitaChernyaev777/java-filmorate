package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class MpaRatingDto {
    private int id;
    private String name;

    public static List<MpaRatingDto> values() {
        return Arrays.asList(
                new MpaRatingDto(1, "G"),
                new MpaRatingDto(2, "PG"),
                new MpaRatingDto(3, "PG-13"),
                new MpaRatingDto(4, "R"),
                new MpaRatingDto(5, "NC-17")
        );
    }
}
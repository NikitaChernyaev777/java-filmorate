package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.GenreDto;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GenreService {

    public List<GenreDto> getAllGenres() {
        return Arrays.stream(Genre.values())
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public GenreDto getGenreById(int id) {
        if (id < 1 || id > Genre.values().length) {
            throw new NotFoundException("Жанр не найден!");
        }
        return toDto(Genre.values()[id - 1]);
    }

    private GenreDto toDto(Genre genre) {
        GenreDto dto = new GenreDto();
        dto.setId(genre.ordinal() + 1);
        dto.setName(genre.getTitle());
        return dto;
    }
}
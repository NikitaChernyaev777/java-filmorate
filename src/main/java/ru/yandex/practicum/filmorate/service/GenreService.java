package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.GenreDbStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreDbStorage genreDbStorage;

    public List<Genre> getAllGenres() {
        return genreDbStorage.findAll();
    }

    public Genre getGenreById(int id) {
        return genreDbStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с id = " + id + " не найден!"));
    }
}
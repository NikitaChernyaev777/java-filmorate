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

    public List<Genre> findAll() {
        return genreDbStorage.findAll();
    }

    public Genre findById(int id) {
        Genre genre = genreDbStorage.findById(id);

        if (genre == null) {
            throw new NotFoundException("Жанр с ID=" + id + " не найден");
        }

        return genre;
    }
}
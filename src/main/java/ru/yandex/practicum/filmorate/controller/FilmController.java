package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public List<Film> getAllFilms() {
        log.info("Получение списка всех фильмов");
        return new ArrayList<>(films.values());
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        log.info("Добавление фильма {}", film.getName());

        film.setId(getNextId());
        films.put(film.getId(), film);

        log.info("Фильм {} с id {} успешно добавлен", film.getName(), film.getId());
        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film newFilm) {
        log.info("Обновление данных фильма {}", newFilm.getName());

        if (newFilm.getId() == null) {
            log.warn("Id не указан!");
            throw new ConditionsNotMetException("Id не указан!");
        }

        Film existingFilm = films.get(newFilm.getId());
        if (existingFilm == null) {
            log.warn("Фильм с id {} не найден!", newFilm.getId());
            throw new NotFoundException("Фильм не найден!");
        }

        updateFilmFields(existingFilm, newFilm);

        log.info("Данные фильма {} с id {} успешно обновлены", existingFilm.getName(), existingFilm.getId());
        return existingFilm;
    }

    private void updateFilmFields(Film existingFilm, Film newFilm) {
        existingFilm.setName(newFilm.getName());

        if (newFilm.getDescription() != null && !newFilm.getDescription().isBlank()) {
            existingFilm.setDescription(newFilm.getDescription());
        }

        if (newFilm.getReleaseDate() != null) {
            existingFilm.setReleaseDate(newFilm.getReleaseDate());
        }

        if (newFilm.getDuration() != null) {
            existingFilm.setDuration(newFilm.getDuration());
        }
    }

    private long getNextId() {
        long currentNextId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentNextId;
    }
}
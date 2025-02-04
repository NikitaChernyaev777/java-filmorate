package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public List<Film> getAllFilms() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film getFilmById(Long id) {
        Film film = films.get(id);
        if (film == null) {
            throw new NotFoundException("Фильм не найден!");
        }
        return film;
    }

    @Override
    public Film addFilm(Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (film.getId() == null) {
            throw new ConditionsNotMetException("Id не указан!");
        }

        Film existingFilm = films.get(film.getId());
        if (existingFilm == null) {
            throw new NotFoundException("Фильм не найден!");
        }

        updateFilmFields(existingFilm, film);
        return existingFilm;
    }

    private void updateFilmFields(Film existingFilm, Film film) {
        existingFilm.setName(film.getName());

        if (film.getDescription() != null && !film.getDescription().isBlank()) {
            existingFilm.setDescription(film.getDescription());
        }

        if (film.getReleaseDate() != null) {
            existingFilm.setReleaseDate(film.getReleaseDate());
        }

        if (film.getDuration() != null) {
            existingFilm.setDuration(film.getDuration());
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
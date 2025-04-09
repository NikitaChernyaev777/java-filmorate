package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {

    Film addFilm(Film film);

    Film updateFilm(Film film);

    Film findById(Long id);

    List<Film> findAll();

    void addLike(Long filmId, Long userId);

    void removeLike(Long filmId, Long userId);

    List<Film> findPopular(Integer count, Integer genreId, Integer year);

    List<Film> findFilmsByDirectorSorted(Long directorId, String sortBy);

    List<Film> getFilmsQuery(String query, List<String> by);

    List<Film>getRecommendations(Long userId);
    void deleteById(Long filmId);
}
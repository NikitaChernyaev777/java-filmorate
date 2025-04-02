package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.GenreDto;
import ru.yandex.practicum.filmorate.model.MpaRatingDto;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;

    @Qualifier("userDbStorage")
    private final UserStorage userStorage;

    public List<Film> getAllFilms() {
        log.info("Получение списка всех фильмов");
        return filmStorage.findAll();
    }

    public Film getFilmById(Long id) {
        log.info("Получение фильма с id {}", id);
        return filmStorage.findById(id);
    }

    public Film addFilm(Film film) {
        log.info("Добавление фильма {}", film.getName());
        validateGenresAndMpaRating(film);
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        log.info("Обновление данных фильма {}", film.getName());
        validateGenresAndMpaRating(film);
        getFilmById(film.getId());
        return filmStorage.updateFilm(film);
    }

    public void addLike(Long filmId, Long userId) {
        filmStorage.findById(filmId);
        userStorage.findById(userId);
        filmStorage.addLike(filmId, userId);
        log.info("Пользователь с id {} успешно поставил лайк фильму с id {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        log.info("Удаление лайка у фильма с id {} от пользователя с id {}", filmId, userId);
        filmStorage.findById(filmId);
        userStorage.findById(userId);
        filmStorage.removeLike(filmId, userId);
        log.info("Пользователь с id {} успешно удалил лайк у фильма с id {}", userId, filmId);
    }

    public List<Film> getMostLikedFilms(int count) {
        log.info("Получение списка наиболее популярных фильмов по количеству лайков");
        return filmStorage.findTopPopular(count);
    }

    private void validateGenresAndMpaRating(Film film) {
        if (film.getMpa() == null || film.getMpa().getId() < 1
                || film.getMpa().getId() > MpaRatingDto.values().size()) {
            throw new NotFoundException("Некорректный идентификатор MPA");
        }

        if (film.getGenres() != null) {
            for (GenreDto genre : film.getGenres()) {
                if (genre.getId() < 1 || genre.getId() > GenreDto.values().size()) {
                    throw new NotFoundException("Некорректный идентификатор жанра " + genre.getName());
                }
            }
        }
    }
}
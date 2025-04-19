package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.GenreDbStorage;
import ru.yandex.practicum.filmorate.dao.MpaRatingDbStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.enums.EventOperation;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaRatingDbStorage mpaRatingDbStorage;
    private final GenreDbStorage genreDbStorage;
    private final FeedStorage feedStorage;
    private final DirectorStorage directorStorage;

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
        feedStorage.addEvent(userId, filmId, EventOperation.ADD, EventType.LIKE);

        log.info("Пользователь с ID={} успешно поставил лайк фильму с ID={}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        log.info("Удаление лайка у фильма с ID={} от пользователя с ID={}", filmId, userId);

        filmStorage.findById(filmId);
        userStorage.findById(userId);
        filmStorage.removeLike(filmId, userId);
        feedStorage.addEvent(userId, filmId, EventOperation.REMOVE, EventType.LIKE);

        log.info("Пользователь с ID={} успешно удалил лайк у фильма с ID={}", userId, filmId);
    }

    public List<Film> getPopularFilms(Integer count, Integer genreId, Integer year) {
        log.info("Получение списка наиболее популярных фильмов по количеству лайков");
        return filmStorage.findPopular(count, genreId, year);
    }

    public List<Film> findFilmsByDirectorSorted(Long directorId, String sortBy) {
        log.info("Получение всех отсортированных фильмов режиссера");

        if (!directorStorage.existsById(directorId)) {
            throw new NotFoundException("Режиссер с ID=" + directorId + "не найден");
        }

        return filmStorage.findFilmsByDirectorSorted(directorId, sortBy);
    }

    public List<Film> getFilmsQuery(String query, List<String> by) {
        log.info("Получение списка фильмов по запросу: {}, параметры {}", query, by);
        return filmStorage.getFilmsQuery(query, by);
    }

    public List<Film> getRecommendations(Long userId) {
        userStorage.findById(userId);
        return filmStorage.getRecommendations(userId);
    }

    public void deleteFilm(Long filmId) {
        log.info("Удаление фильма с ID={}", filmId);
        filmStorage.findById(filmId);
        filmStorage.deleteById(filmId);
    }

    private void validateGenresAndMpaRating(Film film) {
        if (film.getMpa() == null) {
            throw new NotFoundException("MPA рейтинг не может быть null");
        }

        if (!mpaRatingDbStorage.existsById(film.getMpa().getId())) {
            throw new NotFoundException("MPA рейтинг с ID=" + film.getMpa().getId() + " не найден");
        }

        if (film.getGenres() != null) {
            Set<Integer> existingGenreIds = genreDbStorage.findAll().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());

            for (Genre genre : film.getGenres()) {
                if (!existingGenreIds.contains(genre.getId())) {
                    throw new NotFoundException("Жанр с ID=" + genre.getId() + " не найден");
                }
            }
        }
    }

    public List<Film> getCommonFilmsWithFriend(Long userId, Long friendId) {
        log.info("Получение списка общих фильмов 2ух друзей с ID={} и ID={}", userId, friendId);
        return filmStorage.getCommonFilmsWithFriend(userId, friendId);
    }
}
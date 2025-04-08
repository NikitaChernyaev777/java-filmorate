package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.GenreDbStorage;
import ru.yandex.practicum.filmorate.dao.MpaRatingDbStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;
    @Qualifier("userDbStorage")
    private final UserStorage userStorage;
    private final MpaRatingDbStorage mpaRatingDbStorage;
    private final GenreDbStorage genreDbStorage;

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

    public List<Film> getPopularFilms(Integer count, Integer genreId, Integer year) {
        log.info("Получение списка наиболее популярных фильмов по количеству лайков");
        return filmStorage.findPopular(count, genreId, year);
    }

    public List<Film> findFilmsByDirectorSorted(Long directorId, String sortBy) {
        log.info("Получение всех отсортированных фильмов режиссера");
        return filmStorage.findFilmsByDirectorSorted(directorId, sortBy);
    }

    public void  deleteFilm(Long filmId) {
    public List<Film>getFilmsQuery(String query,List<String>by){
        return filmStorage.getFilmsQuery(query,by);
    }

    public void deleteFilm(Long filmId) {
        log.info("Удаление фильма с Id {}", filmId);
        filmStorage.findById(filmId);
        filmStorage.deleteById(filmId);
    }

    private void validateGenresAndMpaRating(Film film) {
        if (film.getMpa() == null) {
            throw new NotFoundException("MPA рейтинг не может быть null");
        }

        if (!mpaRatingDbStorage.existsById(film.getMpa().getId())) {
            throw new NotFoundException("MPA рейтинг с id=" + film.getMpa().getId() + " не найден");
        }

        if (film.getGenres() != null) {
            Set<Integer> existingGenreIds = genreDbStorage.findAll().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());

            for (Genre genre : film.getGenres()) {
                if (!existingGenreIds.contains(genre.getId())) {
                    throw new NotFoundException("Жанр с id=" + genre.getId() + " не найден");
                }
            }
        }
    }
}
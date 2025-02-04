package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public List<Film> getAllFilms() {
        log.info("Получение списка всех фильмов");
        return filmStorage.getAllFilms();
    }

    public Film getFilmById(Long id) {
        log.info("Получение фильма с id {}", id);
        return filmStorage.getFilmById(id);
    }

    public Film addFilm(Film film) {
        log.info("Добавление фильма {}", film.getName());
        Film addedFilm = filmStorage.addFilm(film);
        log.info("Фильм {} с id {} успешно добавлен", film.getName(), film.getId());
        return addedFilm;
    }

    public Film updateFilm(Film film) {
        log.info("Обновление данных фильма {}", film.getName());
        Film updatedFilm = filmStorage.updateFilm(film);
        log.info("Данные фильма {} с id {} успешно обновлены", updatedFilm.getName(), updatedFilm.getId());
        return updatedFilm;
    }

    public void addLike(Long filmId, Long userId) {
        log.info("Добавление лайка фильму с id {} от пользователя с id {}", filmId, userId);
        Film film = filmStorage.getFilmById(filmId);
        User user = userStorage.getUserById(userId);

        if (film.getLikes().contains(userId)) {
            throw new DuplicatedDataException("Пользователь с id " + userId
                    + " уже поставил лайк фильму с id " + filmId + "!");
        }

        film.getLikes().add(userId);
        log.info("Пользователь с id {} успешно поставил лайк фильму с id {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        log.info("Удаление лайка у фильма с id {} от пользователя с id {}", filmId, userId);

        Film film = filmStorage.getFilmById(filmId);
        User user = userStorage.getUserById(userId);

        if (!film.getLikes().contains(userId)) {
            throw new NotFoundException("Лайк от пользователя с id " + userId
                    + " для фильма с id " + filmId + " не найден!");
        }

        film.getLikes().remove(userId);
        log.info("Пользователь с id {} успешно удалил лайк у фильма с id {}", userId, filmId);
    }

    public List<Film> getMostLikedFilms(int count) {
        log.info("Получение списка наиболее популярных фильмов по количеству лайков");
        return filmStorage.getAllFilms().stream()
                .sorted(Comparator.comparing((Film film) -> film.getLikes().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }
}
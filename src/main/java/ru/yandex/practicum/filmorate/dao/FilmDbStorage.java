package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.GenreDto;
import ru.yandex.practicum.filmorate.model.MpaRatingDto;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Primary
@Repository
@RequiredArgsConstructor
@Qualifier("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film addFilm(Film film) {
        String insertFilmSql = "INSERT INTO film (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(insertFilmSql, new String[]{"film_id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        updateFilmGenres(film);
        return findById(film.getId());
    }

    @Override
    public Film updateFilm(Film film) {
        String updateFilmSql = "UPDATE film SET name = ?, description = ?, release_date = ?, duration = ?, " +
                "mpa_id = ? WHERE film_id = ?";

        if (jdbcTemplate.update(updateFilmSql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()) == 0) {
            throw new NotFoundException("Фильм с Id=" + film.getId() + " не найден");
        }

        updateFilmGenres(film);
        return findById(film.getId());
    }

    @Override
    public Film findById(Long id) {
        String findFilmByIdSql = "SELECT f.*, mr.name AS mpa_name FROM film f " +
                "JOIN mpa_rating mr ON f.mpa_id = mr.mpa_id WHERE f.film_id = ?";

        List<Film> films = jdbcTemplate.query(findFilmByIdSql, this::mapToFilm, id);
        if (films.isEmpty()) {
            throw new NotFoundException("Фильм с Id=" + id + " не найден");
        }

        Film film = films.get(0);
        loadGenresForFilm(film);
        loadLikesForFilm(film);
        return film;
    }

    @Override
    public List<Film> findAll() {
        String findAllSql = "SELECT f.*, mr.name AS mpa_name FROM film f " +
                "JOIN mpa_rating mr ON f.mpa_id = mr.mpa_id";

        List<Film> films = jdbcTemplate.query(findAllSql, this::mapToFilm);

        loadGenresForFilms(films);
        loadLikesForFilms(films);
        return films;
    }

    @Override
    public List<Film> findTopPopular(int count) {
        String topFilmsSql = "SELECT f.*, mr.name AS mpa_name, COUNT(fl.user_id) AS like_count " +
                "FROM film f " +
                "JOIN mpa_rating mr ON f.mpa_id = mr.mpa_id " +
                "LEFT JOIN film_like fl ON f.film_id = fl.film_id " +
                "GROUP BY f.film_id " +
                "ORDER BY like_count DESC " +
                "LIMIT ?";

        List<Film> films = jdbcTemplate.query(topFilmsSql, this::mapToFilm, count);

        loadGenresForFilms(films);
        loadLikesForFilms(films);
        return films;
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        String insertLikeSql = "INSERT INTO film_like (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(insertLikeSql, filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        String removeLikeSql = "DELETE FROM film_like WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(removeLikeSql, filmId, userId);
    }

    private Film mapToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("film_id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));

        MpaRatingDto mpaRatingDto = new MpaRatingDto();
        mpaRatingDto.setId(rs.getInt("mpa_id"));
        mpaRatingDto.setName(rs.getString("mpa_name"));
        film.setMpa(mpaRatingDto);

        film.setGenres(new TreeSet<>(Comparator.comparing(GenreDto::getId)));
        film.setLikes(new HashSet<>());
        return film;
    }

    private void updateFilmGenres(Film film) {
        jdbcTemplate.update("DELETE FROM film_genre WHERE film_id = ?", film.getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String sql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
            for (GenreDto genre : film.getGenres()) {
                jdbcTemplate.update(sql, film.getId(), genre.getId());
            }
        }
    }

    private void loadGenresForFilm(Film film) {
        String sql = "SELECT g.* FROM genre g " +
                "JOIN film_genre fg ON g.genre_id = fg.genre_id " +
                "WHERE fg.film_id = ?";

        List<GenreDto> genres = jdbcTemplate.query(sql, (rs, rowNum) -> {
            GenreDto genre = new GenreDto();
            genre.setId(rs.getInt("genre_id"));
            genre.setName(rs.getString("name"));
            return genre;
        }, film.getId());

        film.getGenres().addAll(genres);
    }

    private void loadGenresForFilms(List<Film> films) {
        if (films.isEmpty()) return;

        String sql = "SELECT fg.film_id, g.genre_id, g.name FROM genre g " +
                "JOIN film_genre fg ON g.genre_id = fg.genre_id " +
                "WHERE fg.film_id IN (" +
                String.join(",", Collections.nCopies(films.size(), "?")) + ")";

        List<Object> args = films.stream()
                .map(Film::getId)
                .collect(Collectors.toList());

        Map<Long, List<GenreDto>> filmGenres = new HashMap<>();
        jdbcTemplate.query(sql, args.toArray(), (rs) -> {
            Long filmId = rs.getLong("film_id");
            GenreDto genre = new GenreDto();
            genre.setId(rs.getInt("genre_id"));
            genre.setName(rs.getString("name"));

            filmGenres.computeIfAbsent(filmId, k -> new ArrayList<>()).add(genre);
        });

        films.forEach(film -> {
            List<GenreDto> genres = filmGenres.getOrDefault(film.getId(), new ArrayList<>());
            film.getGenres().addAll(genres);
        });
    }

    private void loadLikesForFilm(Film film) {
        String sql = "SELECT user_id FROM film_like WHERE film_id = ?";
        List<Long> likes = jdbcTemplate.query(sql, (rs, rowNum) ->
                rs.getLong("user_id"), film.getId());
        film.getLikes().addAll(likes);
    }

    private void loadLikesForFilms(List<Film> films) {
        String sql = "SELECT film_id, user_id FROM film_like WHERE film_id IN (" +
                String.join(",", Collections.nCopies(films.size(), "?")) + ")";

        List<Object> args = films.stream()
                .map(Film::getId)
                .collect(Collectors.toList());

        Map<Long, Set<Long>> filmLikes = new HashMap<>();
        jdbcTemplate.query(sql, args.toArray(), (rs) -> {
            Long filmId = rs.getLong("film_id");
            Long userId = rs.getLong("user_id");
            filmLikes.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
        });

        films.forEach(film -> {
            Set<Long> likes = filmLikes.getOrDefault(film.getId(), new HashSet<>());
            film.getLikes().addAll(likes);
        });
    }
}
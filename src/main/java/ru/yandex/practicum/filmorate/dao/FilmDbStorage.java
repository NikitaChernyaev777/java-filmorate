package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Primary
@Repository
@RequiredArgsConstructor
@Qualifier("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final MpaRatingDbStorage mpaRatingDbStorage;

    @Override
    public Film addFilm(Film film) {
        if (!mpaRatingDbStorage.existsById(film.getMpa().getId())) {
            throw new NotFoundException("MPA рейтинг с ID=" + film.getMpa().getId() + " не найден");
        }

        String insertFilmSql = "INSERT INTO film (name, description, release_date, duration, mpa_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(insertFilmSql, new String[]{"film_id"});
            preparedStatement.setString(1, film.getName());
            preparedStatement.setString(2, film.getDescription());
            preparedStatement.setDate(3, Date.valueOf(film.getReleaseDate()));
            preparedStatement.setInt(4, film.getDuration());
            preparedStatement.setInt(5, film.getMpa().getId());
            return preparedStatement;
        }, keyHolder);

        film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            updateFilmGenres(film);
        }

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            updateFilmDirectors(film);
        }

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
            throw new NotFoundException("Фильм с ID=" + film.getId() + " не найден");
        }

        updateFilmGenres(film);

        String deleteDirectorSql = "DELETE FROM film_director WHERE film_id = ?";
        jdbcTemplate.update(deleteDirectorSql, film.getId());

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            updateFilmDirectors(film);
        }

        return findById(film.getId());
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
    public Film findById(Long id) {
        String findFilmByIdSql = "SELECT f.*, mr.name AS mpa_name FROM film f " +
                "JOIN mpa_rating mr ON f.mpa_id = mr.mpa_id WHERE f.film_id = ?";

        List<Film> films = jdbcTemplate.query(findFilmByIdSql, this::mapToFilm, id);
        if (films.isEmpty()) {
            throw new NotFoundException("Фильм с ID=" + id + " не найден");
        }

        Film film = films.get(0);
        loadGenresForFilm(film);
        loadLikesForFilm(film);

        return film;
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        String checkSql = "SELECT COUNT(*) FROM film_like WHERE film_id = ? AND user_id = ?";

        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, filmId, userId);
        if (count != null && count == 0) {
            String insertLikeSql = "INSERT INTO film_like (film_id, user_id) VALUES (?, ?)";
            jdbcTemplate.update(insertLikeSql, filmId, userId);
        }
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        String removeLikeSql = "DELETE FROM film_like WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(removeLikeSql, filmId, userId);
    }

    @Override
    public List<Film> findPopular(Integer count, Integer genreId, Integer year) {
        StringBuilder sql = new StringBuilder(
                "SELECT f.*, mr.name AS mpa_name, COUNT(fl.user_id) AS like_count " +
                        "FROM film f " +
                        "JOIN mpa_rating mr ON f.mpa_id = mr.mpa_id " +
                        "LEFT JOIN film_like fl ON f.film_id = fl.film_id "
        );

        List<Object> parameters = new ArrayList<>();
        List<String> conditions = new ArrayList<>();

        if (genreId != null) {
            sql.append(
                    "LEFT JOIN film_genre fg ON f.film_id = fg.film_id " +
                            "LEFT JOIN genre g ON fg.genre_id = g.genre_id "
            );
            conditions.add("g.genre_id = ?");
            parameters.add(genreId);
        }

        if (year != null) {
            conditions.add("YEAR(f.release_date) = ?");
            parameters.add(year);
        }

        if (!conditions.isEmpty()) {
            sql.append("WHERE ");
            sql.append(String.join(" AND ", conditions));
        }

        sql.append(
                "GROUP BY f.film_id " +
                        "ORDER BY like_count DESC " +
                        "LIMIT ?"
        );
        parameters.add(count);

        List<Film> films = jdbcTemplate.query(sql.toString(), this::mapToFilm, parameters.toArray());

        loadGenresForFilms(films);
        loadLikesForFilms(films);

        return films;
    }


    @Override
    public List<Film> getFilmsQuery(String query, List<String> by) {
        StringBuilder basicSqlQuery = new StringBuilder(
                "SELECT f.*, mr.name AS mpa_name, COUNT(fl.user_id) AS like_count " +
                        "FROM film f " +
                        "JOIN mpa_rating mr ON f.mpa_id = mr.mpa_id " +
                        "LEFT JOIN film_like fl ON f.film_id = fl.film_id "
        );

        List<Object> parameters = new ArrayList<>();
        List<String> conditions = new ArrayList<>();

        if (by != null && query != null) {
            String lowerQuery = "%" + query.toLowerCase() + "%";
            if (by.contains("director")) {
                basicSqlQuery.append(
                        "LEFT JOIN film_director fd ON f.film_id = fd.film_id " +
                                "LEFT JOIN director d ON fd.director_id = d.director_id "
                );
                conditions.add("LOWER(d.name) LIKE ?");
                parameters.add(lowerQuery);
            }

            if (by.contains("title")) {
                conditions.add("LOWER(f.name) LIKE ?");
                parameters.add(lowerQuery);
            }
        }

        if (!conditions.isEmpty()) {
            basicSqlQuery.append("WHERE ");
            basicSqlQuery.append(String.join(" OR ", conditions));
        }

        basicSqlQuery.append(" GROUP BY f.film_id, mpa_name ORDER BY like_count DESC");

        List<Film> films = jdbcTemplate.query(basicSqlQuery.toString(), this::mapToFilm, parameters.toArray());

        loadGenresForFilms(films);
        loadLikesForFilms(films);

        return films;

    }

    @Override
    public List<Film> findFilmsByDirectorSorted(Long directorId, String sortBy) {
        String orderBy;
        switch (sortBy.toLowerCase()) {
            case "likes" -> orderBy = "COUNT(fl.user_id) DESC";
            case "year" -> orderBy = "f.release_date";
            default -> throw new ConditionsNotMetException("Параметр сортировки " + sortBy + " недоступен");
        }

        String sql = "SELECT f.*, mr.name AS mpa_name " +
                "FROM film f " +
                "JOIN mpa_rating mr ON f.mpa_id = mr.mpa_id " +
                "LEFT JOIN film_like fl ON f.film_id = fl.film_id " +
                "JOIN film_director fd ON f.film_id = fd.film_id " +
                "WHERE fd.director_id = ? " +
                "GROUP BY f.film_id " +
                "ORDER BY " + orderBy;

        List<Film> films = jdbcTemplate.query(sql, this::mapToFilm, directorId);

        loadGenresForFilms(films);
        loadLikesForFilms(films);

        return films;
    }

    public List<Film> getRecommendations(Long userId) {
        String similarUserQuery =
                "SELECT f2.user_id " +
                        "FROM film_like f1 " +
                        "JOIN film_like f2 ON f1.film_id = f2.film_id " +
                        "WHERE f1.user_id = ? AND f2.user_id <> f1.user_id " +
                        "GROUP BY f2.user_id " +
                        "ORDER BY COUNT(*) DESC " +
                        "LIMIT 1 ";

        List<Long> similarUsers = jdbcTemplate.query(similarUserQuery, (resultSet, rowNum) ->
                resultSet.getLong("user_id"), userId);

        if (similarUsers.isEmpty()) {
            return new ArrayList<>();
        }

        Long similarUserId = similarUsers.get(0);

        String recommendationFilmsQuery =
                "SELECT f.*, mr.name AS mpa_name, COUNT(fl.user_id) AS like_count " +
                        "FROM film f " +
                        "JOIN mpa_rating mr ON f.mpa_id = mr.mpa_id " +
                        "JOIN film_like fl ON f.film_id = fl.film_id AND fl.user_id = ?" +
                        "LEFT JOIN film_like ml ON f.film_id = ml.film_id AND ml.user_id = ? " +
                        "WHERE ml.user_id IS NULL " +
                        "GROUP BY f.film_id, mr.name";

        List<Film> films = jdbcTemplate.query(recommendationFilmsQuery, this::mapToFilm, similarUserId, userId);

        loadLikesForFilms(films);
        loadGenresForFilms(films);

        return films;
    }

    @Override
    public void deleteById(Long filmId) {
        String deleteFilmFromGenreTableSql = "DELETE FROM film_genre WHERE film_id = ?";
        jdbcTemplate.update(deleteFilmFromGenreTableSql, filmId);

        String deleteLikeSql = "DELETE FROM film_like WHERE film_id = ?";
        jdbcTemplate.update(deleteLikeSql, filmId);

        String deleteFilmByIdSql = "DELETE FROM film WHERE film_id = ?";
        jdbcTemplate.update(deleteFilmByIdSql, filmId);
    }

    private Film mapToFilm(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(resultSet.getLong("film_id"));
        film.setName(resultSet.getString("name"));
        film.setDescription(resultSet.getString("description"));
        film.setReleaseDate(resultSet.getDate("release_date").toLocalDate());
        film.setDuration(resultSet.getInt("duration"));

        MpaRating mpa = mpaRatingDbStorage.findById(resultSet.getInt("mpa_id"));
        film.setMpa(mpa);

        film.setGenres(new TreeSet<>(Comparator.comparing(Genre::getId)));
        film.setLikes(new HashSet<>());
        film.setDirectors(loadDirectors(film.getId()));

        return film;
    }

    private void updateFilmGenres(Film film) {
        jdbcTemplate.update("DELETE FROM film_genre WHERE film_id = ?", film.getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String sql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
            Set<Integer> addedGenreIds = new HashSet<>();

            for (Genre genre : film.getGenres()) {
                if (!addedGenreIds.contains(genre.getId())) {
                    jdbcTemplate.update(sql, film.getId(), genre.getId());
                    addedGenreIds.add(genre.getId());
                }
            }
        }
    }

    private void loadGenresForFilm(Film film) {
        String sql = "SELECT g.* FROM genre g " +
                "JOIN film_genre fg ON g.genre_id = fg.genre_id " +
                "WHERE fg.film_id = ?";

        List<Genre> genres = jdbcTemplate.query(sql, (resultSet, rowNum) ->
                new Genre(resultSet.getInt("genre_id"),
                        resultSet.getString("name")), film.getId());

        film.getGenres().addAll(genres);
    }

    private void loadGenresForFilms(List<Film> films) {
        if (films.isEmpty()) return;

        String sql = "SELECT fg.film_id, g.genre_id, g.name FROM genre g " +
                "JOIN film_genre fg ON g.genre_id = fg.genre_id " +
                "WHERE fg.film_id IN (" +
                String.join(",", Collections.nCopies(films.size(), "?")) + ")";

        List<Long> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toList());

        Map<Long, List<Genre>> filmGenres = new HashMap<>();

        jdbcTemplate.query(sql, preparedStatement -> {
            for (int i = 0; i < filmIds.size(); i++) {
                preparedStatement.setLong(i + 1, filmIds.get(i));
            }
        }, resultSet -> {
            Long filmId = resultSet.getLong("film_id");
            Genre genre = new Genre();
            genre.setId(resultSet.getInt("genre_id"));
            genre.setName(resultSet.getString("name"));
            filmGenres.computeIfAbsent(filmId, k -> new ArrayList<>()).add(genre);
        });

        films.forEach(film -> {
            List<Genre> genres = filmGenres.getOrDefault(film.getId(), new ArrayList<>());
            film.getGenres().addAll(genres);
        });
    }

    private void loadLikesForFilm(Film film) {
        String sql = "SELECT user_id FROM film_like WHERE film_id = ?";

        List<Long> likes = jdbcTemplate.query(sql, (resultSet, rowNum) ->
                resultSet.getLong("user_id"), film.getId());
        film.getLikes().addAll(likes);
    }

    private void loadLikesForFilms(List<Film> films) {
        String sql = "SELECT film_id, user_id FROM film_like WHERE film_id IN (" +
                String.join(",", Collections.nCopies(films.size(), "?")) + ")";

        List<Long> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toList());

        Map<Long, Set<Long>> filmLikes = new HashMap<>();

        jdbcTemplate.query(sql, preparedStatement -> {
            for (int i = 0; i < filmIds.size(); i++) {
                preparedStatement.setLong(i + 1, filmIds.get(i));
            }
        }, (resultSet) -> {
            Long filmId = resultSet.getLong("film_id");
            Long userId = resultSet.getLong("user_id");
            filmLikes.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
        });

        films.forEach(film -> {
            Set<Long> likes = filmLikes.getOrDefault(film.getId(), new HashSet<>());
            film.getLikes().addAll(likes);
        });
    }

    private void updateFilmDirectors(Film film) {
        jdbcTemplate.update("DELETE FROM film_director WHERE film_id = ?", film.getId());

        String insertSql = "INSERT INTO film_director (film_id, director_id) VALUES (?, ?)";
        for (Director director : film.getDirectors()) {
            jdbcTemplate.update(insertSql, film.getId(), director.getId());
        }
    }

    private Set<Director> loadDirectors(Long filmId) {
        String sql = "SELECT d.director_id, d.name FROM film_director fd " +
                "JOIN director d ON fd.director_id = d.director_id " +
                "WHERE fd.film_id = ?";

        return new HashSet<>(jdbcTemplate.query(sql, (resultSet, rowNum) ->
                new Director(resultSet.getLong("director_id"),
                        resultSet.getString("name")), filmId));
    }

    @Override
    public List<Film> getCommonFilmsWithFriend(Long userId, Long friendId) {
        String sql = "SELECT f.* " +
                "FROM film f " +
                "WHERE f.film_id IN (" +
                "   SELECT film_id " +
                "   FROM film_like " +
                "   WHERE user_Id = ? OR user_id = ? " +
                "   GROUP BY film_id " +
                "   HAVING COUNT(DISTINCT user_id) = 2" +
                ")";

        List<Film> films = jdbcTemplate.query(sql, this::mapToFilm, userId, friendId);

        loadGenresForFilms(films);
        loadLikesForFilms(films);

        return films;
    }
}
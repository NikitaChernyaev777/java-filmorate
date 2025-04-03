package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.dao.MpaRatingDbStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({FilmDbStorage.class, MpaRatingDbStorage.class})
class FilmDbStorageTest {

    @Autowired
    private FilmDbStorage filmStorage;

    private Film testFilm;

    @BeforeEach
    void setUp() {
        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        mpa.setName("G");

        testFilm = new Film();
        testFilm.setName("Test Film");
        testFilm.setDescription("Test Description");
        testFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        testFilm.setDuration(120);
        testFilm.setMpa(mpa);
        testFilm.setGenres(new TreeSet<>());
        testFilm.setLikes(new HashSet<>());
    }

    @Test
    void shouldAddFilm() {
        Film addedFilm = filmStorage.addFilm(testFilm);
        assertThat(addedFilm.getId()).isNotNull();
        assertThat(addedFilm.getName()).isEqualTo("Test Film");
    }

    @Test
    void shouldUpdateFilm() {
        Film addedFilm = filmStorage.addFilm(testFilm);
        addedFilm.setName("Updated Name");
        addedFilm.setDescription("Updated Description");

        Film updatedFilm = filmStorage.updateFilm(addedFilm);
        assertThat(updatedFilm.getName()).isEqualTo("Updated Name");
        assertThat(updatedFilm.getDescription()).isEqualTo("Updated Description");
    }

    @Test
    void shouldFindFilmById() {
        Film addedFilm = filmStorage.addFilm(testFilm);
        Film foundFilm = filmStorage.findById(addedFilm.getId());
        assertThat(foundFilm).isEqualTo(addedFilm);
    }

    @Test
    void shouldFindAllFilms() {
        filmStorage.addFilm(testFilm);
        List<Film> films = filmStorage.findAll();
        assertThat(films).hasSize(1);
    }

    @Test
    void shouldThrowWhenFilmNotFound() {
        assertThrows(NotFoundException.class, () -> filmStorage.findById(999L));
    }
}
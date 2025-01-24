package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class FilmControllerTest {

    private Validator validator;

    @BeforeEach
    public void setUp() {
        try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            validator = validatorFactory.getValidator();
        }
    }

    private Film createValidFilm() {
        Film film = new Film();
        film.setName("Taxi 3");
        film.setDescription("Out to stop a new gang disguised as Santa Claus, Emilien and Daniel must also "
                + "handle major changes in their personal relationships.");
        film.setReleaseDate(LocalDate.of(2003, 1, 29));
        film.setDuration(84);
        return film;
    }

    @Test
    public void shouldPassWhenValidRequiredFields() {
        Film film = createValidFilm();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(0, violations.size(),
                "Ожидалось, что фильм с корректными данными пройдет валидацию");
    }

    @Test
    public void shouldFailWhenNameIsBlank() {
        Film film = createValidFilm();
        film.setName(" ");
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Фильм без названия не должен проходить валидацию");
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    public void shouldFailWhenDurationIsNotPositive(int duration) {
        Film film = createValidFilm();
        film.setDuration(duration);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(),
                "Продолжительность фильма должна быть положительным числом");
    }

    @Test
    public void shouldFailWhenDescriptionExceedsMaxLength() {
        Film film = createValidFilm();
        film.setDescription("Q".repeat(201));
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(),
                "писание длиной более 200 символов не должно проходить валидацию");
    }

    @Test
    public void shouldFailWhenReleaseDateIsBeforeAllowedDate() {
        Film film = createValidFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(),
                "Дата релиза фильма раньше 28 декабря 1895 года не должна проходить валидацию");
    }
}
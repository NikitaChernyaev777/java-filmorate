package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class UserControllerTest {

    private Validator validator;

    @BeforeEach
    public void setUp() {
        try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            validator = validatorFactory.getValidator();
        }
    }

    private User createValidUser() {
        return new User();
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "invalid_email", "mail.ru"})
    public void shouldFailWhenInvalidEmail(String email) {
        User user = createValidUser();
        user.setEmail(email);
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(),
                "Некорректный электронный адрес '" + email + "' не должен проходить валидацию");
    }

    @Test
    public void shouldFailWhenSpacesInLogin() {
        User user = createValidUser();
        user.setLogin("Log in");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Логин с пробелами не должен проходить валидацию");
    }
}
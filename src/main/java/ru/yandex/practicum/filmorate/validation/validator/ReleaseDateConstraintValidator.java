package ru.yandex.practicum.filmorate.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.yandex.practicum.filmorate.validation.annotation.ReleaseDateConstraint;

import java.time.LocalDate;

public class ReleaseDateConstraintValidator implements ConstraintValidator<ReleaseDateConstraint, LocalDate> {

    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return !value.isBefore(MIN_RELEASE_DATE);
    }
}
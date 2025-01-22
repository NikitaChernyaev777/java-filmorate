package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class User {

    private Long id;

    @NotBlank(message = "Электронная почта не может быть пустой!")
    @Email(message = "Электронная почта должна содержать символ '@'!")
    private String email;

    @NotBlank(message = "Логин не может быть пустым!")
    @Pattern(regexp = "\\S+$", message = "Логин не может содержать пробелы!")
    private String login;

    private String name;

    @PastOrPresent(message = "Дата рождения не может быть указана в будущем времени!")
    private LocalDate birthday;
}
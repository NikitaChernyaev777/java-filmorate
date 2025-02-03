package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ToString
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

    private Set<Long> friends = new HashSet<>();

    public User(String email, String login, String name, LocalDate birthday) {
        this.email = email;
        this.login = login;
        this.name = (name == null || name.isBlank()) ? login : name;
        this.birthday = birthday;
    }
}
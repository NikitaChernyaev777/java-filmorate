package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class User {

    private Long id;

    @NotBlank(message = "Электронная почта не может быть пустой!")
    @Email(message = "Электронная почта должна содержать символ '@'!")
    private String email;

    @NotBlank(message = "Логин не может быть пустым!")
    @Pattern(regexp = "\\S+$", message = "Логин не может содержать пробелы!")
    private String login;

    private String name;

    @NotNull
    @PastOrPresent(message = "Дата рождения не может быть указана в будущем времени!")
    private LocalDate birthday;

    private Set<Long> friends = new HashSet<>();

    private Map<Long, FriendshipStatus> friendshipStatuses = new HashMap<>();

    public void setName(String name) {
        this.name = (name == null || name.isBlank()) ? this.login : name;
    }
}
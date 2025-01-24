package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final Map<Long, User> users = new HashMap<>();
    private final Set<String> emails = new HashSet<>();

    @GetMapping
    public List<User> getAllUsers() {
        log.info("Получение списка всех пользователей");
        return new ArrayList<>(users.values());
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        log.info("Создание нового пользователя {}", user.getLogin());

        if (emails.contains(user.getEmail())) {
            log.warn("Электронная почта {} уже используется!", user.getEmail());
            throw new DuplicatedDataException("Данная электронная почта уже используется!");
        }

        user.setId(getNextId());
        users.put(user.getId(), user);
        emails.add(user.getEmail());

        log.info("Новый пользователь {} с id {} успешно создан", user.getLogin(), user.getId());
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User newUser) {
        log.info("Обновление данных пользователя {}", newUser.getLogin());

        if (newUser.getId() == null) {
            log.warn("Id не указан!");
            throw new ConditionsNotMetException("Id не указан!");
        }

        User existingUser = users.get(newUser.getId());
        if (existingUser == null) {
            log.warn("Пользователь с id {} не найден!", newUser.getId());
            throw new NotFoundException("Пользователь не найден!");
        }

        if (!existingUser.getEmail().equals(newUser.getEmail()) && emails.contains(newUser.getEmail())) {
            log.warn("Электронная почта {} уже используется, она не может быть обновлена!", newUser.getEmail());
            throw new DuplicatedDataException("Данная электронная почта уже используется!");
        }

        emails.remove(existingUser.getEmail());
        updateUserFields(existingUser, newUser);
        emails.add(existingUser.getEmail());

        log.info("Данные пользователя {} с id {} спешно обновлены", existingUser.getLogin(), existingUser.getId());
        return existingUser;
    }

    private void updateUserFields(User existingUser, User newUser) {
        existingUser.setEmail(newUser.getEmail());
        existingUser.setLogin(newUser.getLogin());

        existingUser.setName((newUser.getName() == null || newUser.getName().trim().isEmpty())
                ? newUser.getLogin()
                : newUser.getName());

        if (newUser.getBirthday() != null) {
            existingUser.setBirthday(newUser.getBirthday());
        }
    }

    private long getNextId() {
        long currentNextId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentNextId;
    }
}
package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private final Set<String> emails = new HashSet<>();

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User getUserById(Long id) {
        User user = users.get(id);
        if (user == null) {
            throw new NotFoundException("Пользователь не найден!");
        }
        return user;
    }

    @Override
    public User createUser(User user) {
        if (emails.contains(user.getEmail())) {
            throw new DuplicatedDataException("Данная электронная почта уже используется!");
        }

        user.setId(getNextId());
        users.put(user.getId(), user);
        emails.add(user.getEmail());

        return user;
    }

    @Override
    public User updateUser(User user) {
        if (user.getId() == null) {
            throw new ConditionsNotMetException("Id не указан!");
        }

        User existingUser = users.get(user.getId());
        if (existingUser == null) {
            throw new NotFoundException("Пользователь не найден!");
        }

        if (!existingUser.getEmail().equals(user.getEmail()) && emails.contains(user.getEmail())) {
            throw new DuplicatedDataException("Данная электронная почта уже используется!");
        }

        emails.remove(existingUser.getEmail());
        updateUserFields(existingUser, user);
        emails.add(existingUser.getEmail());

        return existingUser;
    }

    private void updateUserFields(User existingUser, User user) {
        existingUser.setEmail(user.getEmail());
        existingUser.setLogin(user.getLogin());

        existingUser.setName((user.getName() == null || user.getName().trim().isEmpty())
                ? user.getLogin()
                : user.getName());

        if (user.getBirthday() != null) {
            existingUser.setBirthday(user.getBirthday());
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
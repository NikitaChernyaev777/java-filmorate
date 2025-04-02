package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    @Qualifier("userDbStorage")
    private final UserStorage userStorage;

    public List<User> getAllUsers() {
        log.info("Получение списка всех пользователей");
        return userStorage.findAll();
    }

    public User getUserById(Long id) {
        log.info("Получение пользователя с id {}", id);
        return userStorage.findById(id);
    }

    public User addUser(User user) {
        log.info("Создание нового пользователя {}", user.getLogin());
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        log.info("Обновление данных пользователя {}", user.getLogin());
        getUserById(user.getId());
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.updateUser(user);
    }

    public void addFriend(Long userId, Long friendId) {
        log.info("Добавление Пользователя с id {} в друзья к пользователю с id {}", userId, friendId);
        if (!userStorage.existsById(userId) || !userStorage.existsById(friendId)) {
            throw new NotFoundException("Пользователь не найден!");
        }
        userStorage.addFriend(userId, friendId);
        log.info("Пользователи с id {} и {} успешно добавлены друг к другу в друзья", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        log.info("Удаление пользователя с id {} из друзей пользователя с id {}", userId, friendId);
        if (!userStorage.existsById(userId) || !userStorage.existsById(friendId)) {
            throw new NotFoundException("Пользователь не найден!");
        }
        userStorage.removeFriend(userId, friendId);
        log.info("Пользователи с id {} и {} успешно удалены друг у друга из друзей", userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        log.info("Получение списка друзей пользователя с id {}", userId);
        userStorage.findById(userId);
        return userStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        log.info("Получение списка общих друзей для пользователей с id {} и {}", userId, otherUserId);
        userStorage.findById(userId);
        userStorage.findById(otherUserId);
        return userStorage.getCommonFriends(userId, otherUserId);
    }
}
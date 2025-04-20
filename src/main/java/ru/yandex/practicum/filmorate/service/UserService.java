package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.enums.EventOperation;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;
    private final FeedStorage feedStorage;

    public List<User> getAllUsers() {
        log.info("Получение списка всех пользователей");
        return userStorage.findAll();
    }

    public User getUserById(Long id) {
        log.info("Получение пользователя с ID={}", id);
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
        log.info("Добавление пользователя с ID={} в друзья к пользователю с ID={}", userId, friendId);

        if (!userStorage.existsById(userId) || !userStorage.existsById(friendId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        userStorage.addFriend(userId, friendId);
        feedStorage.addEvent(userId, friendId, EventOperation.ADD, EventType.FRIEND);

        log.info("Пользователи с ID={} и ID={} успешно добавлены друг к другу в друзья", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        log.info("Удаление пользователя с ID={} из друзей пользователя с ID={}", userId, friendId);

        if (!userStorage.existsById(userId) || !userStorage.existsById(friendId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        userStorage.removeFriend(userId, friendId);
        feedStorage.addEvent(userId, friendId, EventOperation.REMOVE, EventType.FRIEND);

        log.info("Пользователи с ID={} и ID={} успешно удалены друг у друга из друзей", userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        log.info("Получение списка друзей пользователя с ID={}", userId);
        userStorage.findById(userId);
        return userStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        log.info("Получение списка общих друзей для пользователей с ID={} и ID={}", userId, otherUserId);

        userStorage.findById(userId);
        userStorage.findById(otherUserId);

        return userStorage.getCommonFriends(userId, otherUserId);
    }

    public void deleteUser(Long userId) {
        log.info("Удаление пользователя с ID={}", userId);

        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID=" + userId + " не найден");
        }

        userStorage.deleteById(userId);
    }

    public List<Feed> findByUser(Long userId) {
        getUserById(userId);
        return feedStorage.findByUser(userId);
    }
}
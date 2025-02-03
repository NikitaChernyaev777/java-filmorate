package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public List<User> getAllUsers() {
        log.info("Получение списка всех пользователей");
        return userStorage.getAllUsers();
    }

    public User getUserById(Long id) {
        log.info("Получение пользователя с id {}", id);
        return userStorage.getUserById(id);
    }

    public User createUser(User user) {
        log.info("Создание нового пользователя {}", user.getLogin());
        User createdUser = userStorage.createUser(user);
        log.info("Новый пользователь {} с id {} успешно создан", user.getLogin(), user.getId());
        return createdUser;
    }

    public User updateUser(User user) {
        log.info("Обновление данных пользователя {}", user.getLogin());
        User updatedUser = userStorage.updateUser(user);
        log.info("Данные пользователя {} с id {} успешно обновлены", updatedUser.getLogin(), updatedUser.getId());
        return updatedUser;
    }

    public void addFriend(Long userId, Long friendId) {
        log.info("Добавление Пользователя с id {} в друзья к пользователю с id {}", userId, friendId);

        if (userId.equals(friendId)) {
            throw new ConditionsNotMetException("Нельзя добавить в друзья самого себя!");
        }

        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);

        if (user.getFriends().contains(friendId)) {
            throw new DuplicatedDataException("Пользователь с id " + friendId
                    + " уже является другом пользователя с id " + userId + "!");
        }

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        log.info("Пользователи с id {} и {} успешно добавлены друг к другу в друзья", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        log.info("Удаление пользователя с id {} из друзей пользователя с id {}", userId, friendId);
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        log.info("Пользователи с id {} и {} успешно удалены друг у друга из друзей", userId, friendId);
    }

    public List<User> getListOfFriends(Long userId) {
        log.info("Получение списка друзей пользователя с id {}", userId);
        User user = userStorage.getUserById(userId);

        return user.getFriends().stream()
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }

    public List<User> getListOfCommonFriends(Long userId, Long otherUserId) {
        log.info("Получение списка общих друзей для пользователей с id {} и {}", userId, otherUserId);
        User user = userStorage.getUserById(userId);
        User otherUser = userStorage.getUserById(otherUserId);

        Set<Long> commonFriendIds = new HashSet<>(user.getFriends());
        commonFriendIds.retainAll(otherUser.getFriends());

        return commonFriendIds.stream()
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }
}
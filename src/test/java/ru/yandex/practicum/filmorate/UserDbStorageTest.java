package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@mail.ru")
                .login("testLogin")
                .name("Test Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
    }

    @Test
    void shouldUpdateUser() {
        User addedUser = userStorage.addUser(testUser);
        User updatedUser = User.builder()
                .id(addedUser.getId())
                .email("updated@mail.ru")
                .login("updatedLogin")
                .name("Updated Name")
                .birthday(LocalDate.of(1995, 5, 5))
                .build();

        User result = userStorage.updateUser(updatedUser);

        assertThat(result)
                .isNotNull()
                .isEqualToComparingFieldByField(updatedUser);
    }

    @Test
    void shouldFindAllUsers() {
        userStorage.addUser(testUser);
        User anotherUser = User.builder()
                .email("another@mail.ru")
                .login("anotherLogin")
                .name("Another Name")
                .birthday(LocalDate.of(1995, 5, 5))
                .build();
        userStorage.addUser(anotherUser);

        List<User> users = userStorage.findAll();

        assertThat(users)
                .hasSize(2)
                .extracting(User::getLogin)
                .containsExactlyInAnyOrder("testLogin", "anotherLogin");
    }

    @Test
    void shouldAddAndRemoveFriend() {
        User user1 = userStorage.addUser(testUser);
        User user2 = userStorage.addUser(
                User.builder()
                        .email("friend@mail.ru")
                        .login("friendLogin")
                        .name("Friend Name")
                        .birthday(LocalDate.of(1995, 5, 5))
                        .build());

        userStorage.addFriend(user1.getId(), user2.getId());
        List<User> friends = userStorage.getFriends(user1.getId());

        assertThat(friends)
                .hasSize(1)
                .extracting(User::getId)
                .containsExactly(user2.getId());

        userStorage.removeFriend(user1.getId(), user2.getId());
        assertThat(userStorage.getFriends(user1.getId())).isEmpty();
    }

    @Test
    void shouldFindCommonFriends() {
        User user1 = userStorage.addUser(testUser);
        User user2 = userStorage.addUser(
                User.builder()
                        .email("user2@mail.ru")
                        .login("user2Login")
                        .name("User 2")
                        .birthday(LocalDate.of(1995, 5, 5))
                        .build());
        User commonFriend = userStorage.addUser(
                User.builder()
                        .email("common@mail.ru")
                        .login("commonFriend")
                        .name("Common Friend")
                        .birthday(LocalDate.of(1993, 3, 3))
                        .build());

        userStorage.addFriend(user1.getId(), commonFriend.getId());
        userStorage.addFriend(user2.getId(), commonFriend.getId());

        List<User> commonFriends = userStorage.getCommonFriends(user1.getId(), user2.getId());

        assertThat(commonFriends)
                .hasSize(1)
                .extracting(User::getId)
                .containsExactly(commonFriend.getId());
    }

    @Test
    void shouldCheckUserExists() {
        User user = userStorage.addUser(testUser);

        assertTrue(userStorage.existsById(user.getId()));
        assertFalse(userStorage.existsById(999L));
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        assertThrows(NotFoundException.class, () -> userStorage.findById(999L));

        User unknownUser = User.builder()
                .id(999L)
                .email("unknown@mail.ru")
                .login("unknown")
                .birthday(LocalDate.now())
                .build();
        assertThrows(NotFoundException.class, () -> userStorage.updateUser(unknownUser));
    }
}
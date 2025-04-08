package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@Primary
@Repository
@RequiredArgsConstructor
@Qualifier("userDbStorage")
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public User addUser(User user) {
        String insertUserSql = "INSERT INTO app_user (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(insertUserSql, new String[]{"user_id"});
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, java.sql.Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return user;
    }

    @Override
    public User updateUser(User user) {
        String updateUserSql = "UPDATE app_user SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";

        if (jdbcTemplate.update(updateUserSql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()) == 0) {
            throw new NotFoundException("Пользователь с Id=" + user.getId() + " не найден");
        }
        return user;
    }

    @Override
    public User findById(Long id) {
        String findUserByIdSql = "SELECT * FROM app_user WHERE user_id = ?";

        List<User> users = jdbcTemplate.query(findUserByIdSql, this::mapToUser, id);
        if (users.isEmpty()) {
            throw new NotFoundException("Пользователь с Id=" + id + " не найден");
        }

        return users.get(0);
    }

    @Override
    public List<User> findAll() {
        String findAllUsersSql = "SELECT * FROM PUBLIC.app_user";
        return jdbcTemplate.query(findAllUsersSql, this::mapToUser);
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        String insertFriendSql = "INSERT INTO friendship (user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(insertFriendSql, userId, friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        String removeFriendSql = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(removeFriendSql, userId, friendId);
    }

    @Override
    public List<User> getFriends(Long userId) {
        String listOfFriendsSql = "SELECT u.* FROM app_user u " +
                "JOIN friendship f ON u.user_id = f.friend_id " +
                "WHERE f.user_id = ?";
        return jdbcTemplate.query(listOfFriendsSql, this::mapToUser, userId);
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        String listOfCommonFriendsSql = "SELECT u.* FROM app_user u " +
                "JOIN friendship f1 ON u.user_id = f1.friend_id " +
                "JOIN friendship f2 ON f1.friend_id = f2.friend_id " +
                "WHERE f1.user_id = ? AND f2.user_id = ?";
        return jdbcTemplate.query(listOfCommonFriendsSql, this::mapToUser, userId, otherUserId);
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM app_user WHERE user_id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, id) > 0;
    }

    @Override
    public void deleteById(Long userId) {
        String deleteUserFromFriendshipTableSql = "DELETE FROM friendship WHERE user_id = ? OR friend_id = ?";
        jdbcTemplate.update(deleteUserFromFriendshipTableSql, userId, userId);

        String deleteUserByIdSql = "DELETE FROM app_user WHERE user_id = ?";
        jdbcTemplate.update(deleteUserByIdSql, userId);
    }

    private User mapToUser(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("user_id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());
        return user;
    }
}
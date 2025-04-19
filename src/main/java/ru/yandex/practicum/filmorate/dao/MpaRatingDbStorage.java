package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MpaRatingDbStorage {

    private final JdbcTemplate jdbcTemplate;

    public List<MpaRating> findAll() {
        String sql = "SELECT * FROM mpa_rating";
        return jdbcTemplate.query(sql, this::mapToMpa);
    }

    public MpaRating findById(int id) {
        String findByIdSql = "SELECT * FROM mpa_rating WHERE mpa_id = ?";
        List<MpaRating> ratings = jdbcTemplate.query(findByIdSql, this::mapToMpa, id);
        return ratings.isEmpty() ? null : ratings.get(0);
    }

    private MpaRating mapToMpa(ResultSet resultSet, int rowNum) throws SQLException {
        return new MpaRating(resultSet.getInt("mpa_id"), resultSet.getString("name"));
    }

    public boolean existsById(int id) {
        String sql = "SELECT COUNT(*) FROM mpa_rating WHERE mpa_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }
}
package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MpaRatingDbStorage {

    private final JdbcTemplate jdbcTemplate;

    public List<MpaRating> findAll() {
        String sql = "SELECT * FROM mpa_rating";
        return jdbcTemplate.query(sql, this::mapRowToMpa);
    }

    public Optional<MpaRating> findById(int id) {
        String sql = "SELECT * FROM mpa_rating WHERE mpa_id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, this::mapRowToMpa, id));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    private MpaRating mapRowToMpa(ResultSet rs, int rowNum) throws SQLException {
        return new MpaRating(rs.getInt("mpa_id"), rs.getString("name"));
    }

    public boolean existsById(int id) {
        String sql = "SELECT COUNT(*) FROM mpa_rating WHERE mpa_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }
}
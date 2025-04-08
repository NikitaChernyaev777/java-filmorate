package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Director> findAll() {
        String findAllSql = "SELECT * FROM director";
        return jdbcTemplate.query(findAllSql, (rs, rowNum) ->
                new Director(rs.getLong("director_id"), rs.getString("name")));
    }

    @Override
    public Optional<Director> findById(long id) {
        String findByIdSql = "SELECT * FROM director WHERE director_id = ?";
        List<Director> directors = jdbcTemplate.query(findByIdSql, (rs, rowNum) ->
                new Director(rs.getLong("director_id"), rs.getString("name")), id);
        return directors.stream().findFirst();
    }

    @Override
    public Director addDirector(Director director) {
        String insertDirectorSql = "INSERT INTO director (name) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(insertDirectorSql, new String[]{"director_id"});
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);

        director.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());

        return director;
    }

    @Override
    public Director updateDirector(Director director) {
        String updateDirectorSql = "UPDATE director SET name = ? WHERE director_id = ?";
        jdbcTemplate.update(updateDirectorSql, director.getName(), director.getId());
        return director;
    }

    @Override
    public void deleteById(Long id) {
        String deleteByIdSql = "DELETE FROM director WHERE director_id = ?";
        jdbcTemplate.update(deleteByIdSql, id);
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM director WHERE director_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }
}
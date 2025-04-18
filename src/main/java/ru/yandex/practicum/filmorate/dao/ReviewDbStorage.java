package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Review addReview(Review review) {
        String insertReviewSql = "INSERT INTO review ( film_id, user_id, content, is_positive, useful) " +
                "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(insertReviewSql, new String[]{"review_id"});
            ps.setLong(1, review.getFilmId());
            ps.setLong(2, review.getUserId());
            ps.setString(3, review.getContent());
            ps.setBoolean(4, review.getIsPositive());
            ps.setInt(5, review.getUseful());
            return ps;
        }, keyHolder);

        review.setReviewId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return review;
    }

    @Override
    public Optional<Review> updateReview(Review review) {
        String updateReviewSql = "UPDATE review SET content = ?, is_positive = ? WHERE review_id = ?";
        jdbcTemplate.update(updateReviewSql, review.getContent(), review.getIsPositive(), review.getReviewId());
        return findById(review.getReviewId());
    }

    @Override
    public void deleteById(Long reviewId) {
        String deleteByIdSql = "DELETE FROM review WHERE review_id = ?";
        jdbcTemplate.update(deleteByIdSql, reviewId);
    }

    @Override
    public Optional<Review> findById(Long reviewId) {
        String sql = "SELECT * FROM review WHERE review_id = ?";
        List<Review> reviews = jdbcTemplate.query(sql, this::mapToReview, reviewId);
        return reviews.stream().findFirst();
    }

    @Override
    public List<Review> findAllByFilmId(Long filmId, int count) {
        String findByFilmIdSql = filmId != null
                ? "SELECT * FROM review WHERE film_id = ? ORDER BY useful DESC LIMIT ?"
                : "SELECT * FROM review ORDER BY useful DESC LIMIT ?";
        return filmId != null
                ? jdbcTemplate.query(findByFilmIdSql, this::mapToReview, filmId, count)
                : jdbcTemplate.query(findByFilmIdSql, this::mapToReview, count);
    }

    @Override
    public List<Review> findAll(int count) {
        String findAllSql = "SELECT r.*, COALESCE(SUM(rl.is_useful), 0) AS useful " +
                "FROM review r " +
                " LEFT JOIN review_like rl ON r.review_id = rl.review_id " +
                "GROUP BY r.review_id " +
                "ORDER BY useful DESC " +
                "LIMIT ?";
        return jdbcTemplate.query(findAllSql, this::mapToReview, count);
    }

    @Override
    public void addLike(Long reviewId, Long userId) {
        removeReaction(reviewId, userId);
        String addLikeSql = "INSERT INTO review_like (review_id, user_id, is_useful) VALUES (?, ?, TRUE)";
        jdbcTemplate.update(addLikeSql, reviewId, userId);
        incrementUseful(reviewId);
    }

    @Override
    public void addDislike(Long reviewId, Long userId) {
        removeReaction(reviewId, userId);
        String addDislikeSql = "INSERT INTO review_like (review_id, user_id, is_useful) VALUES (?, ?, FALSE)";
        jdbcTemplate.update(addDislikeSql, reviewId, userId);
        decrementUseful(reviewId);
    }

    @Override
    public void removeReaction(Long reviewId, Long userId) {
        Boolean wasUseful = getRatingType(reviewId, userId);
        jdbcTemplate.update("DELETE FROM review_like WHERE review_id = ? AND user_id = ?", reviewId, userId);
        if (wasUseful != null) {
            if (wasUseful) {
                decrementUseful(reviewId);
            } else {
                incrementUseful(reviewId);
            }
        }
    }

    private Boolean getRatingType(Long reviewId, Long userId) {
        String sql = "SELECT is_useful FROM review_like WHERE review_id = ? AND user_id = ?";
        List<Boolean> result = jdbcTemplate.query(sql,
                (rs, rowNum) -> rs.getBoolean("is_useful"), reviewId, userId);
        return result.isEmpty() ? null : result.get(0);
    }

    private void incrementUseful(Long reviewId) {
        String incrementUsefulSql = "UPDATE review SET useful = useful + 1 WHERE review_id = ?";
        jdbcTemplate.update(incrementUsefulSql, reviewId);
    }

    private void decrementUseful(Long reviewId) {
        String decrementUsefulSql = "UPDATE review SET useful = useful - 1 WHERE review_id = ?";
        jdbcTemplate.update(decrementUsefulSql, reviewId);
    }

    private Review mapToReview(ResultSet rs, int rowNum) throws SQLException {
        Review review = new Review();
        review.setReviewId(rs.getLong("review_id"));
        review.setFilmId(rs.getLong("film_id"));
        review.setUserId(rs.getLong("user_id"));
        review.setContent(rs.getString("content"));
        review.setIsPositive(rs.getBoolean("is_positive"));
        review.setUseful(rs.getInt("useful"));

        return review;
    }
}
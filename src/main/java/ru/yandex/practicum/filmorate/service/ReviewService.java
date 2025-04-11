package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public Review addReview(Review review) {
        log.info("Добавление отзыва с Id {}", review.getReviewId());
        validateReview(review);
        return reviewStorage.addReview(review);
    }

    public Review updateReview(Review review) {
        log.info("Обновление отзыва с Id {}", review.getReviewId());
        validateReview(review);
        return reviewStorage.updateReview(review);
    }

    public void deleteReview(Long reviewId) {
        log.info("Удаление отзыва с reviewId {}", reviewId);
        reviewStorage.deleteById(reviewId);
    }

    public Review findById(Long reviewId) {
        log.info("Получение отзыва с Id {}", reviewId);
        return reviewStorage.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв с ID " + reviewId + " не найден"));
    }

    public List<Review> findReviews(Long filmId, int count) {
        log.info("Получения списка отзывов");
        return (filmId != null)
                ? reviewStorage.findAllByFilmId(filmId, count)
                : reviewStorage.findAll(count);
    }

    public void addLike(Long reviewId, Long userId) {
        log.info("Добавление пользователем {} лайка отзыву с id {}", userId, reviewId);
        reviewStorage.addLike(reviewId, userId);
    }

    public void addDislike(Long reviewId, Long userId) {
        log.info("Добавление пользователем {} дизлайка отзыву с id {}", userId, reviewId);
        reviewStorage.addDislike(reviewId, userId);
    }

    public void removeReaction(Long reviewId, Long userId) {
        reviewStorage.removeReaction(reviewId, userId);
    }

    private void validateReview(Review review) {
        userStorage.findById(review.getUserId());
        filmStorage.findById(review.getFilmId());
    }
}
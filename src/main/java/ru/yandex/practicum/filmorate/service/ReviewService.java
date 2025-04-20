package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.enums.EventOperation;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;
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
    private final FeedStorage feedStorage;

    public Review addReview(Review review) {
        validateReview(review);

        Review addReview = reviewStorage.addReview(review);
        log.info("Добавление отзыва с ID={}", addReview.getReviewId());
        feedStorage.addEvent(addReview.getUserId(), addReview.getReviewId(), EventOperation.ADD, EventType.REVIEW);

        return addReview;
    }

    public Review updateReview(Review review) {
        validateReview(review);

        Review updateReview = reviewStorage.updateReview(review);
        log.info("Обновление отзыва с ID={}", review.getReviewId());
        if (updateReview == null) {
            throw new NotFoundException("Отзыв с ID=" + review.getReviewId() + " не найден");
        }

        feedStorage.addEvent(updateReview.getUserId(), updateReview.getReviewId(), EventOperation.UPDATE,
                EventType.REVIEW);

        return updateReview;
    }


    public List<Review> findReviews(Long filmId, int count) {
        log.info("Получения списка отзывов");
        return (filmId != null)
                ? reviewStorage.findAllByFilmId(filmId, count)
                : reviewStorage.findAll(count);
    }

    public Review findById(Long reviewId) {
        log.info("Получение отзыва с ID={}", reviewId);
        return findReviewOrThrow(reviewId);
    }

    public void deleteReview(Long reviewId) {
        log.info("Удаление отзыва с ID={}", reviewId);
        Review review = findReviewOrThrow(reviewId);

        feedStorage.addEvent(review.getUserId(), review.getReviewId(), EventOperation.REMOVE, EventType.REVIEW);
        reviewStorage.deleteById(reviewId);
    }

    public void addLike(Long reviewId, Long userId) {
        log.info("Добавление пользователем с ID={} лайка отзыву с ID={}", userId, reviewId);
        reviewStorage.addLike(reviewId, userId);
    }

    public void addDislike(Long reviewId, Long userId) {
        log.info("Добавление пользователем с ID={} дизлайка отзыву с ID={}", userId, reviewId);
        reviewStorage.addDislike(reviewId, userId);
    }

    public void removeReaction(Long reviewId, Long userId) {
        reviewStorage.removeReaction(reviewId, userId);
    }

    private Review findReviewOrThrow(Long reviewId) {
        Review review = reviewStorage.findById(reviewId);
        if (review == null) {
            throw new NotFoundException("Отзыв с ID=" + reviewId + " не найден");
        }
        return review;
    }

    private void validateReview(Review review) {
        userStorage.findById(review.getUserId());
        filmStorage.findById(review.getFilmId());
    }
}
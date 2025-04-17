package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.event.FeedEventType;
import ru.yandex.practicum.filmorate.model.event.FeedOperation;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final FeedService feedService;

    public Review addReview(Review review) {
        validateReview(review);
        Review reviewNew = reviewStorage.addReview(review);
        log.info("Добавление отзыва с Id {}", reviewNew.getReviewId());
        feedService.add(reviewNew.getUserId(), reviewNew.getReviewId(), FeedEventType.REVIEW, FeedOperation.ADD);
        return reviewNew;
    }

    public Review updateReview(Review review) {
        log.info("Обновление отзыва с Id {}", review.getReviewId());
        validateReview(review);
        feedService.add(review.getUserId(), review.getReviewId(), FeedEventType.REVIEW, FeedOperation.UPDATE);
        return reviewStorage.updateReview(review);
    }

    public void deleteReview(Long reviewId) {
        log.info("Удаление отзыва с reviewId {}", reviewId);
        List<Review> review = reviewStorage.findById(reviewId).stream().collect(Collectors.toList());
        feedService.add(review.getFirst().getUserId(), review.getFirst().getReviewId(), FeedEventType.REVIEW, FeedOperation.REMOVE);
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
        feedService.add(userId, reviewId, FeedEventType.LIKE, FeedOperation.ADD);
    }

    public void addDislike(Long reviewId, Long userId) {
        log.info("Добавление пользователем {} дизлайка отзыву с id {}", userId, reviewId);
        reviewStorage.addDislike(reviewId, userId);
        feedService.add(userId, reviewId, FeedEventType.LIKE, FeedOperation.ADD);
    }

    public void removeReaction(Long reviewId, Long userId) {
        reviewStorage.removeReaction(reviewId, userId);
        feedService.add(userId, reviewId, FeedEventType.LIKE, FeedOperation.REMOVE);
    }

    private void validateReview(Review review) {
        userStorage.findById(review.getUserId());
        filmStorage.findById(review.getFilmId());
    }
}
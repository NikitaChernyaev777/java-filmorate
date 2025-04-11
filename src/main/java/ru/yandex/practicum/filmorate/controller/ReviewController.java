package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public List<Review> findAll(@RequestParam(required = false) Long filmId,
                                @RequestParam(defaultValue = "10") int count) {
        return reviewService.findReviews(filmId, count);
    }

    @GetMapping("/{id}")
    public Review findReviewById(@PathVariable Long id) {
        return reviewService.findById(id);
    }

    @PostMapping
    public Review addReview(@RequestBody @Valid Review review) {
        return reviewService.addReview(review);
    }

    @PutMapping
    public Review updateReview(@RequestBody @Valid Review review) {
        return reviewService.updateReview(review);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.addLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.addDislike(id, userId);
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.removeReaction(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDislike(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.removeReaction(id, userId);
    }
}
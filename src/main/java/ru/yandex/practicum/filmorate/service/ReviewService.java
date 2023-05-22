package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ReviewNotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.util.Collection;
import java.util.Optional;

@Service
@Slf4j
public class ReviewService {
    private final ReviewStorage reviewStorage;

    public ReviewService(ReviewStorage reviewStorage) {
        this.reviewStorage = reviewStorage;
    }

    public Review create(Review review) {
        Review createdReview = reviewStorage.createReview(review);
        log.info("Добавлен новый отзыв: {}.", createdReview);
        return createdReview;
    }

    public Review update(Review review) {
        Review updatedReview = reviewStorage.updateReview(review);
        log.info("Обновлен отзыв: {}.", updatedReview);
        return updatedReview;
    }

    public void delete(long id) {
        reviewStorage.deleteReview(id);
    }

    public Review findById(long id) {
        Optional<Review> reviewOptional = reviewStorage.findReviewById(id);
        if (reviewOptional.isEmpty()) {
            throw new ReviewNotFoundException("Отзыв с ID = " + id + " не найден.");
        }
        log.info("Запрошен отзыв с ID = {}.", reviewOptional.get().getReviewId());
        return reviewOptional.get();
    }

    public Collection<Review> findReviewByIdFilm(Long filmId, int count) {
        Collection<Review> reviews;
        if (filmId == null) {
            reviews = reviewStorage.findReviewByCount(count);
            log.info("Возвращено {} отзывов.", reviews.size());
        } else {
            reviews = reviewStorage.findReviewByIdFilm(filmId, count);
            log.info("Возвращено {} отзывов для фильма с ID = {}.", reviews.size(), filmId);
        }
        return reviews;

    }

    public Review addLike(long reviewId, long userId) {
        Optional<Review> reviewOptional = reviewStorage.addLike(reviewId, userId);
        if (reviewOptional.isEmpty()) {
            throw new ReviewNotFoundException("Отзыв с ID = " + reviewId + " не найден.");
        }
        return reviewOptional.get();
    }

    public Review addDislike(long reviewId, long userId) {
        Optional<Review> reviewOptional = reviewStorage.addDislike(reviewId, userId);
        if (reviewOptional.isEmpty()) {
            throw new ReviewNotFoundException("Отзыв с ID = " + reviewId + " не найден.");
        }
        return reviewOptional.get();
    }

    public Review removeLike(long reviewId, long userId) {
        Optional<Review> reviewOptional = reviewStorage.removeLike(reviewId, userId);
        if (reviewOptional.isEmpty()) {
            throw new ReviewNotFoundException("Отзыв с ID = " + reviewId + " не найден.");
        }
        return reviewOptional.get();
    }

    public Review removeDislike(long reviewId, long userId) {
        Optional<Review> reviewOptional = reviewStorage.removeDislike(reviewId, userId);
        if (reviewOptional.isEmpty()) {
            throw new ReviewNotFoundException("Отзыв с ID = " + reviewId + " не найден.");
        }
        return reviewOptional.get();
    }
}

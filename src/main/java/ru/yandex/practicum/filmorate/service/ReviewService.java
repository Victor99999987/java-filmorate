package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ReviewNotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiFunction;

@Service
@Slf4j
public class ReviewService {
    private final ReviewStorage reviewStorage;

    public ReviewService(ReviewStorage reviewStorage) {
        this.reviewStorage = reviewStorage;
    }

    public Review create(Review review) {
        Review createdReview = reviewStorage.add(review);
        log.info("Добавлен новый отзыв: {}.", createdReview);
        return createdReview;
    }

    public Review update(Review review) {
        Review updatedReview = reviewStorage.update(review);
        log.info("Обновлен отзыв: {}.", updatedReview);
        return updatedReview;
    }

    public Review delete(long id) {
        return reviewStorage.remove(id);
    }

    public Review findById(long id) {
        log.info("Запрошен отзыв с ID = {}.", id);
        return reviewStorage.getById(id);
    }

    public Collection<Review> findReviewByIdFilm(Long filmId, int count) {
        Collection<Review> reviews;
        if (filmId == null) {
            reviews = reviewStorage.getReviewByCount(count);
            log.info("Возвращено {} отзывов.", reviews.size());
        } else {
            reviews = reviewStorage.getReviewByIdFilm(filmId, count);
            log.info("Возвращено {} отзывов для фильма с ID = {}.", reviews.size(), filmId);
        }
        return reviews;
    }

    public Review addLike(long reviewId, long userId) {
        return updateReview(reviewId, userId,
                reviewStorage::addLike, "Отзыв с ID = " + reviewId + " не найден.");
    }

    public Review addDislike(long reviewId, long userId) {
        return updateReview(reviewId, userId,
                reviewStorage::addDislike, "Отзыв с ID = " + reviewId + " не найден.");
    }

    public Review removeLike(long reviewId, long userId) {
        return updateReview(reviewId, userId,
                reviewStorage::removeLike, "Отзыв с ID = " + reviewId + " не найден.");
    }

    public Review removeDislike(long reviewId, long userId) {
        return updateReview(reviewId, userId,
                reviewStorage::removeDislike, "Отзыв с ID = " + reviewId + " не найден.");
    }

    private Review updateReview(long reviewId, long userId, BiFunction<Long, Long, Optional<Review>> function, String errorMessage) {
        Optional<Review> reviewOptional = function.apply(reviewId, userId);
        if (reviewOptional.isEmpty()) {
            throw new ReviewNotFoundException(errorMessage + reviewId + " не найден.");
        }
        return reviewOptional.get();
    }
}

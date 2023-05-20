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
        Optional<Review> review = reviewStorage.findReviewById(id);
        if (review.isEmpty()) {
            throw new ReviewNotFoundException("Отзыв с ID = " + id + " не найден.");
        }
        log.info("Запрошен отзыв с ID = {}.", review.get().getReviewId());
        return review.get();
    }

    public Collection<Review> findReviewByIdFilm(Long filmId, int count) {
        Collection<Review> reviews;
        if (filmId == null) {
            reviews = reviewStorage.findReviewByCount(count);
            log.info("Возвращено {} отзывов.",reviews.size());
        } else {
            reviews = reviewStorage.findReviewByIdFilm(filmId, count);
            log.info("Возвращено {} отзывов для фильма с ID = {}.", reviews.size(),filmId);
        }
        return reviews;

    }

    public void addLike(long reviewId, long userId) {
        reviewStorage.addLike(reviewId, userId);
        log.info("Пользователь с ID = {} поставил лайк отзыву с ID = {}.", userId, reviewId);
    }

    public void addDislike(long reviewId, long userId) {
        reviewStorage.addDislike(reviewId, userId);
        log.info("Пользователь с ID = {} поставил дизлайк отзыву с ID = {}.", userId, reviewId);
    }

    public void removeLike(long reviewId, long userId) {
        reviewStorage.removeLike(reviewId, userId);

    }

    public void removeDislike(long reviewId, long userId) {
        reviewStorage.removeDislike(reviewId, userId);
    }
}

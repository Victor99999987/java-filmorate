package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ReviewNotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.type.EventType;
import ru.yandex.practicum.filmorate.model.type.OperationType;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.function.BiFunction;

@Service
@Slf4j
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final Storage<Event> eventStorage;
    private final FilmStorage filmStorage;
    private final Storage<User> userStorage;

    public ReviewService(@Qualifier("DbReviewStorage") ReviewStorage reviewStorage,
                         @Qualifier("DbEventStorage") Storage<Event> eventStorage,
                         @Qualifier("DbFilmStorage") FilmStorage filmStorage,
                         @Qualifier("DbUserStorage") Storage<User> userStorage) {
        this.reviewStorage = reviewStorage;
        this.eventStorage = eventStorage;
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Review create(Review review) {
        userStorage.getById(review.getUserId());
        filmStorage.getById(review.getFilmId());
        Review createdReview = reviewStorage.add(review);
        log.info("Добавлен новый отзыв: {}.", createdReview);
        Event event = Event.builder()
                .timestamp(Instant.now().toEpochMilli())
                .userId(createdReview.getUserId())
                .operation(OperationType.ADD)
                .eventType(EventType.REVIEW)
                .entityId(createdReview.getReviewId())
                .build();
        eventStorage.add(event);
        return createdReview;
    }

    public Review update(Review review) {
        Review updatedReview = reviewStorage.update(review);
        log.info("Обновлен отзыв: {}.", updatedReview);
        Event event = Event.builder()
                .timestamp(Instant.now().toEpochMilli())
                .userId(updatedReview.getUserId())
                .operation(OperationType.UPDATE)
                .eventType(EventType.REVIEW)
                .entityId(updatedReview.getReviewId())
                .build();
        eventStorage.add(event);
        return updatedReview;
    }

    public Review delete(long id) {

        Review review = reviewStorage.remove(id);
        Event event = Event.builder()
                .timestamp(Instant.now().toEpochMilli())
                .userId(review.getUserId())
                .operation(OperationType.REMOVE)
                .eventType(EventType.REVIEW)
                .entityId(review.getReviewId())
                .build();
        eventStorage.add(event);
        return review;
    }

    public Review findById(long id) {
        log.info("Запрошен отзыв с ID = {}.", id);
        return reviewStorage.getById(id);
    }

    public Collection<Review> findAllReviews(Long filmId, int count) {
        Collection<Review> reviews;
        if (filmId == null) {
            reviews = reviewStorage.getReviewByCount(count);
            log.info("Возвращено {} отзывов.", reviews.size());
        } else {
            filmStorage.getById(filmId);
            reviews = reviewStorage.getReviewByIdFilm(filmId, count);
            log.info("Возвращено {} отзывов для фильма с ID = {}.", reviews.size(), filmId);
        }
        return reviews;
    }

    public Review addLike(long reviewId, long userId) {
        userStorage.getById(userId);
        return updateReview(reviewId, userId,
                reviewStorage::addLike, "Отзыв с ID = " + reviewId + " не найден.");
    }

    public Review addDislike(long reviewId, long userId) {
        userStorage.getById(userId);
        return updateReview(reviewId, userId,
                reviewStorage::addDislike, "Отзыв с ID = " + reviewId + " не найден.");
    }

    public Review removeLike(long reviewId, long userId) {
        userStorage.getById(userId);
        return updateReview(reviewId, userId,
                reviewStorage::removeLike, "Отзыв с ID = " + reviewId + " не найден.");
    }

    public Review removeDislike(long reviewId, long userId) {
        userStorage.getById(userId);
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

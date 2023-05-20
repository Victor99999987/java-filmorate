package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;
import java.util.Optional;

public interface ReviewStorage {
    Review createReview(Review review);
    Review updateReview(Review review);
    void deleteReview(long id);
    Optional<Review> findReviewById(long id);
    Collection<Review> findReviewByCount(int count);
    Collection<Review> findReviewByIdFilm(Long filmId, int count);
    void addLike(long reviewId, long userId);
    void addDislike(long reviewId, long userId);
    void removeLike(long reviewId, long userId);
    void removeDislike(long reviewId, long userId);
}

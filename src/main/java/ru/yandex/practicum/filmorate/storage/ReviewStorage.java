package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;
import java.util.Optional;

public interface ReviewStorage extends Storage<Review> {

    Collection<Review> getReviewByCount(int count);

    Collection<Review> getReviewByIdFilm(Long filmId, int count);

    Optional<Review> addLike(long reviewId, long userId);

    Optional<Review> addDislike(long reviewId, long userId);

    Optional<Review> removeLike(long reviewId, long userId);

    Optional<Review> removeDislike(long reviewId, long userId);
}

package ru.yandex.practicum.filmorate.storage.impl.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.ReviewIncorrectLikeException;
import ru.yandex.practicum.filmorate.exception.ReviewNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Repository
@Qualifier("DbReviewStorage")
public class DbReviewStorage extends DbStorage implements ReviewStorage {

    public DbReviewStorage(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    private final String insertLikeReview = "INSERT INTO REVIEWS_LIKES (REVIEW_ID, USER_ID, IS_LIKE) " +
            "VALUES (?, ?, ?)";
    private final String deleteLikeReview = "DELETE FROM REVIEWS_LIKES WHERE REVIEW_ID = ? AND USER_ID = ?";
    private final String updateUsefulPlus = "UPDATE REVIEWS SET USEFUL = USEFUL + 1 WHERE REVIEW_ID = ?";
    private final String updateUsefulMinus = "UPDATE REVIEWS SET USEFUL = USEFUL - 1 WHERE REVIEW_ID = ?";

    @Override
    public List<Review> getAll() {
        String findAllReviews = "SELECT * FROM REVIEWS ORDER BY USEFUL DESC";
        return jdbcTemplate.query(findAllReviews, (rs, rowNum) -> buildReview(rs));
    }

    @Override
    public Review getById(Long id) {
        try {
            String findById = "SELECT * FROM REVIEWS WHERE REVIEW_ID = ?";
            return jdbcTemplate.queryForObject(findById, (rs, rowNum) -> buildReview(rs), id);
        } catch (EmptyResultDataAccessException e) {
            throw new ReviewNotFoundException("Отзыв с ID = " + id + " не найден.");
        }
    }

    @Override
    public Review add(Review review) {
        verifyFilm(review.getFilmId());
        verifyUser(review.getUserId());
        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO REVIEWS (FILM_ID, USER_ID, CONTENT, IS_POSITIVE) values(?, ?, ?, ?)";
        jdbcTemplate.update(conn -> {
            PreparedStatement preparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setLong(1, review.getFilmId());
            preparedStatement.setLong(2, review.getUserId());
            preparedStatement.setString(3, review.getContent());
            preparedStatement.setBoolean(4, review.getIsPositive());
            return preparedStatement;
        }, generatedKeyHolder);
        review.setReviewId(Objects.requireNonNull(generatedKeyHolder.getKey()).longValue());
        return review;
    }

    @Override
    public Review update(Review review) {
        String updateReview = "UPDATE REVIEWS SET CONTENT = ?, IS_POSITIVE = ? WHERE REVIEW_ID = ?";
        boolean reply = jdbcTemplate.update(updateReview,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId()) < 1;
        if (reply) {
            throw new ReviewNotFoundException("Ошибка при обновлении отзыва с ID = " + review.getReviewId() + ".");
        }
        return getById(review.getReviewId());
    }

    @Override
    public Review remove(Long id) {
        String deleteReview = "DELETE FROM REVIEWS WHERE REVIEW_ID = ?";
        Review review = getById(id);
        if (jdbcTemplate.update(deleteReview, id) < 1) {
            throw new ReviewNotFoundException("Ошибка при удалении отзыва с ID = " + id + ".");
        } else {
            log.info("Отзыв с ID = {} удален.", id);
        }
        return review;
    }

    @Override
    public Collection<Review> getReviewByCount(int count) {
        String findAllReviews = "SELECT * FROM REVIEWS ORDER BY USEFUL DESC LIMIT ?";
        return jdbcTemplate.query(findAllReviews, (rs, rowNum) -> buildReview(rs), count);
    }

    @Override
    public Collection<Review> getReviewByIdFilm(Long filmId, int count) {
        String findAllReviewsByFilm = "SELECT * FROM REVIEWS WHERE FILM_ID = ? " +
                "ORDER BY USEFUL DESC LIMIT ?";
        verifyFilm(filmId);
        return jdbcTemplate.query(findAllReviewsByFilm, (rs, rowNum) -> buildReview(rs), filmId, count);
    }

    @Override
    public Optional<Review> addLike(long reviewId, long userId) {
        verifyUser(userId);
        verifyReview(reviewId);
        try {
            if (jdbcTemplate.update(insertLikeReview, reviewId, userId, true) > 0) {
                jdbcTemplate.update(updateUsefulPlus, reviewId);
                log.info("Пользователь с ID = {} добавил лайк для отзыва ID = {}.", userId, reviewId);
                return Optional.ofNullable(getById(reviewId));
            } else {
                return Optional.empty();
            }
        } catch (DuplicateKeyException e) {
            log.info("Ошибка при добавлении лайка для отзыва ID = {} от пользователя с ID = {}.", reviewId, userId);
            throw new ReviewIncorrectLikeException(String
                    .format("Ошибка при добавлении лайка для отзыва ID = %d от пользователя с ID = %d.",
                            reviewId, userId));
        }
    }

    @Override
    public Optional<Review> addDislike(long reviewId, long userId) {
        verifyUser(userId);
        verifyReview(reviewId);
        try {
            if (jdbcTemplate.update(insertLikeReview, reviewId, userId, false) > 0) {
                jdbcTemplate.update(updateUsefulMinus, reviewId);
                log.info("Пользователь с ID = {} добавил дизлайк для отзыва ID = {}.", userId, reviewId);
                return Optional.ofNullable(getById(reviewId));
            } else {
                return Optional.empty();
            }
        } catch (DuplicateKeyException e) {
            log.info("Ошибка при добавлении дизлайка для отзыва ID = {} от пользователя с ID = {}.", reviewId, userId);
            throw new ReviewIncorrectLikeException(String
                    .format("Ошибка при добавлении дизлайка для отзыва ID = %d от пользователя с ID = %d.",
                            reviewId, userId));
        }
    }

    @Override
    public Optional<Review> removeLike(long reviewId, long userId) {
        verifyUser(userId);
        verifyReview(reviewId);
        if (verifyLike(reviewId, userId)) {
            if (jdbcTemplate.update(deleteLikeReview, reviewId, userId) < 1) {
                log.info("Ошибка при удалении лайка для отзыва ID = {} от пользователя с ID = {}.", reviewId, userId);
                throw new ReviewIncorrectLikeException(String
                        .format("Ошибка при удалении лайка для отзыва ID = %d от пользователя с ID = %d.",
                                reviewId, userId));
            } else {
                jdbcTemplate.update(updateUsefulMinus, reviewId);
                log.info("Пользователь с ID = {} удалил лайк для отзыва ID = {}.", userId, reviewId);
                return Optional.ofNullable(getById(reviewId));
            }
        } else {
            throw new ReviewIncorrectLikeException("Удаляемый лайк не является лайком.");
        }
    }

    @Override
    public Optional<Review> removeDislike(long reviewId, long userId) {
        verifyUser(userId);
        verifyReview(reviewId);
        if (!verifyLike(reviewId, userId)) {
            if (jdbcTemplate.update(deleteLikeReview, reviewId, userId) < 1) {
                log.info("Ошибка при удалении дизлайка для отзыва ID = {} от пользователя с ID = {}.", reviewId, userId);
                throw new ReviewIncorrectLikeException(String
                        .format("Ошибка при удалении дизлайка для отзыва ID = %d от пользователя с ID = %d.",
                                reviewId, userId));
            } else {
                jdbcTemplate.update(updateUsefulPlus, reviewId);
                log.info("Пользователь с ID = {} удалил дизлайк для отзыва ID = {}.", userId, reviewId);
                return Optional.ofNullable(getById(reviewId));
            }
        } else {
            throw new ReviewIncorrectLikeException("Удаляемый дизлайк не является дизлайком.");
        }
    }

    private Review buildReview(ResultSet rs) throws SQLException {
        return Review.builder()
                .reviewId(rs.getLong("review_id"))
                .content(rs.getString("content"))
                .isPositive(rs.getBoolean("is_positive"))
                .userId(rs.getLong("user_id"))
                .filmId(rs.getLong("film_id"))
                .useful(rs.getInt("useful"))
                .build();
    }

    private void verifyUser(long userId) {
        String findUser = "SELECT ID FROM USERS WHERE ID = ?";
        SqlRowSet rsUser = jdbcTemplate.queryForRowSet(findUser, userId);
        if (!rsUser.next()) {
            throw new UserNotFoundException("Не найден пользователь с ID = " + userId);
        }
    }

    private void verifyFilm(long filmId) {
        String findFilm = "SELECT ID FROM FILMS WHERE ID = ?";
        SqlRowSet rsFilm = jdbcTemplate.queryForRowSet(findFilm, filmId);
        if (!rsFilm.next()) {
            throw new FilmNotFoundException("Не найден фильм с ID = " + filmId);
        }
    }

    public void verifyReview(long reviewId) {
        Optional<Review> reviewOptional = Optional.ofNullable(getById(reviewId));
        if (reviewOptional.isEmpty()) {
            throw new ReviewNotFoundException("Отзыв с ID = " + reviewId + " не найден.");
        }
    }

    public boolean verifyLike(long reviewId, long userId) {
        String verifyLike = "SELECT IS_LIKE FROM REVIEWS_LIKES WHERE REVIEW_ID = ? AND USER_ID = ?";
        return Boolean.TRUE
                .equals(jdbcTemplate.queryForObject(verifyLike, Boolean.class, reviewId, userId));
    }
}
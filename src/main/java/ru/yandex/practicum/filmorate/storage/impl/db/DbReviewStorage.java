package ru.yandex.practicum.filmorate.storage.impl.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.ReviewNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Repository
@Qualifier("DbReviewStorage")
public class DbReviewStorage extends DbStorage implements ReviewStorage {

    public DbReviewStorage(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    private final String INSERT_LIKE_REVIEW = "INSERT INTO REVIEWS_LIKES (REVIEW_ID, USER_ID, IS_LIKE) " +
            "VALUES (?, ?, ?)";
    private final String DELETE_LIKE_REVIEW = "DELETE FROM REVIEWS_LIKES WHERE REVIEW_ID = ? AND USER_ID = ?";
    private final String UPDATE_USEFUL_PLUS = "UPDATE REVIEWS SET USEFUL = USEFUL + 1 WHERE REVIEW_ID = ?";
    private final String UPDATE_USEFUL_MINUS = "UPDATE REVIEWS SET USEFUL = USEFUL - 1 WHERE REVIEW_ID = ?";

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

    private void findUser(long userId) {
        String FIND_USER = "SELECT ID FROM USERS WHERE ID = ?";
        SqlRowSet rsUser = jdbcTemplate.queryForRowSet(FIND_USER, userId);
        if (!rsUser.next()) {
            throw new UserNotFoundException("Не найден пользователь с ID = " + userId);
        }
    }

    private void findFilm(long filmId) {
        String FIND_FILM = "SELECT ID FROM FILMS WHERE ID = ?";
        SqlRowSet rsFilm = jdbcTemplate.queryForRowSet(FIND_FILM, filmId);
        if (!rsFilm.next()) {
            throw new FilmNotFoundException("Не найден фильм с ID = " + filmId);
        }
    }

    @Override
    public Review createReview(Review review) {
        findFilm(review.getFilmId());
        findUser(review.getUserId());
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
    public Review updateReview(Review review) {
        String UPDATE_REVIEW = "UPDATE REVIEWS SET CONTENT = ?, IS_POSITIVE = ? WHERE REVIEW_ID = ?";
        boolean reply = jdbcTemplate.update(UPDATE_REVIEW,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId()) < 1;
        if (reply) {
            throw new ReviewNotFoundException("Ошибка при обновлении отзыва с ID = " + review.getReviewId() + ".");
        }
        return findReviewById(review.getReviewId()).get();
    }

    @Override
    public void deleteReview(long id) {
        String DELETE_REVIEW = "DELETE FROM REVIEWS WHERE REVIEW_ID = ?";
        if (jdbcTemplate.update(DELETE_REVIEW, id) < 1) {
            throw new ReviewNotFoundException("Ошибка при удалении отзыва с ID = " + id + ".");
        } else {
            log.info("Отзыв с ID = {} удален.", id);
        }
    }

    @Override
    public Optional<Review> findReviewById(long id) {
        try {
            String FIND_BY_ID = "SELECT * FROM REVIEWS WHERE REVIEW_ID = ?";
            Review review = jdbcTemplate.queryForObject(FIND_BY_ID, (rs, rowNum) -> buildReview(rs), id);
            return Optional.ofNullable(review);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Collection<Review> findReviewByCount(int count) {
        String FIND_ALL_REVIEWS = "SELECT * FROM REVIEWS ORDER BY USEFUL DESC LIMIT ?";
        return jdbcTemplate.query(FIND_ALL_REVIEWS, (rs, rowNum) -> buildReview(rs), count);
    }

    @Override
    public Collection<Review> findReviewByIdFilm(Long filmId, int count) {
        String FIND_ALL_REVIEWS_BY_FILM = "SELECT * FROM REVIEWS WHERE FILM_ID = ? " +
                "ORDER BY USEFUL DESC LIMIT ?";
        return jdbcTemplate.query(FIND_ALL_REVIEWS_BY_FILM, (rs, rowNum) -> buildReview(rs), filmId, count);
    }

    @Override
    public void addLike(long reviewId, long userId) {
        findUser(userId);
        jdbcTemplate.update(INSERT_LIKE_REVIEW, reviewId, userId, true);
        jdbcTemplate.update(UPDATE_USEFUL_PLUS, reviewId);
    }

    @Override
    public void addDislike(long reviewId, long userId) {
        findUser(userId);
        jdbcTemplate.update(INSERT_LIKE_REVIEW, reviewId, userId, false);
        jdbcTemplate.update(UPDATE_USEFUL_MINUS, reviewId);
    }

    @Override
    public void removeLike(long reviewId, long userId) {
        if (jdbcTemplate.update(DELETE_LIKE_REVIEW, reviewId, userId) < 1) {
            log.info("Ошибка при удалении лайка для ревью ID = {} от пользователя с ID = {}.", reviewId, userId);
        } else {
            jdbcTemplate.update(UPDATE_USEFUL_MINUS, reviewId);
            log.info("Пользователь с ID = {} удалил лайк для ревью ID = {}.", userId, reviewId);
        }
    }

    @Override
    public void removeDislike(long reviewId, long userId) {
        if (jdbcTemplate.update(DELETE_LIKE_REVIEW, reviewId, userId) < 1) {
            log.info("Ошибка при удалении дизлайка для ревью ID = {} от пользователя с ID = {}.", reviewId, userId);
        } else {
            jdbcTemplate.update(UPDATE_USEFUL_PLUS, reviewId);
            log.info("Пользователь с ID = {} удалил дизлайк для ревью ID = {}.", userId, reviewId);
        }
    }
}

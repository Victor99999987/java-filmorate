package ru.yandex.practicum.filmorate.storage.impl.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ReviewNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationUserException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

@Slf4j
@Repository
@Qualifier("DbReviewStorage")
public class DbReviewStorage extends DbStorage implements ReviewStorage {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    public DbReviewStorage(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    private final static String FIND_BY_ID = "SELECT * FROM REVIEWS WHERE REVIEW_ID = ?";
    private final static String FIND_ALL_REVIEWS = "SELECT * FROM REVIEWS ORDER BY USEFUL DESC LIMIT ?";
    private final static String FIND_ALL_REVIEWS_BY_FILM = "SELECT * FROM REVIEWS WHERE FILM_ID = ? " +
            "ORDER BY USEFUL DESC LIMIT ?";

    private final static String UPDATE_REVIEW = "UPDATE REVIEWS SET CONTENT = ?, IS_POSITIVE = ? WHERE REVIEW_ID = ?";

    private final static String DELETE_REVIEW = "DELETE FROM REVIEWS WHERE REVIEW_ID = ?";

    private final static String INSERT_LIKE_REVIEW = "INSERT INTO REVIEWS_LIKES (REVIEW_ID, USER_ID, IS_LIKE) " +
            "VALUES (?, ?, ?)";
    private final static String DELETE_LIKE_REVIEW = "DELETE FROM REVIEWS_LIKES WHERE REVIEW_ID = ? AND USER_ID = ?";
    private final static String UPDATE_USEFUL_PLUS = "UPDATE REVIEWS SET USEFUL = USEFUL + 1 WHERE REVIEW_ID = ?";
    private final static String UPDATE_USEFUL_MINUS = "UPDATE REVIEWS SET USEFUL = USEFUL - 1 WHERE REVIEW_ID = ?";
    private final static String FIND_USER = "SELECT USER_ID FROM USERS WHERE USER_ID = ?";
    private final static String FIND_FILM = "SELECT FILM_ID FROM FILMS WHERE FILM_ID = ?";

    public static final RowMapper<Review> REVIEW_ROW_MAPPER = (ResultSet resultSet, int rowNum) -> Review.builder()
            .reviewId(resultSet.getLong("review_id"))
            .content(resultSet.getString("content"))
            .isPositive(resultSet.getBoolean("is_positive"))
            .userId(resultSet.getLong("user_id"))
            .filmId(resultSet.getLong("film_id"))
            .build();



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
        SqlRowSet rsUser = jdbcTemplate.queryForRowSet(FIND_USER, userId);
        if (!rsUser.next()) {
            throw new UserNotFoundException("Не найден пользователь с ID = " + userId);
        }
    }

    private void findFilm(long filmId) {
        SqlRowSet rsFilm = jdbcTemplate.queryForRowSet(FIND_FILM, filmId);
        if (!rsFilm.next()) {
            throw new FilmNotFoundException("Не найден фильм с ID = " + filmId);
        }
    }

    @Override
    public Review createReview(Review review) {
        System.out.println("Это метод создания ревью в БД");
        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
        System.out.println("Сгенерирован кихолдер = " + generatedKeyHolder);
        String sql = "INSERT INTO REVIEWS (FILM_ID, USER_ID, CONTENT, IS_POSITIVE) values(?, ?, ?, ?)";
        System.out.println("Создан шаблон строки SQL = " + sql);
        jdbcTemplate.update(conn -> {
            System.out.println("Внутри лямбды");
            PreparedStatement preparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            System.out.println("возвращен сгенерированный ключ — " + preparedStatement);
            preparedStatement.setLong(1, review.getFilmId());
            System.out.println("В стейтмент записан филм айди = " + review.getFilmId());
            preparedStatement.setLong(2, review.getUserId());
            System.out.println("В стейтмент записан юзер айди = " + review.getUserId());
            preparedStatement.setString(3, review.getContent());
            System.out.println("В стейтмент записан контент = " + review.getContent());
            preparedStatement.setBoolean(4, review.isPositive());
            System.out.println("В стейтмент записан позитив = " + review.isPositive());
            return preparedStatement;
        }, generatedKeyHolder);
        System.out.println("Вышли из лямбды");
        review.setReviewId(Objects.requireNonNull(generatedKeyHolder.getKey()).longValue());
        System.out.println("В объект ревью записан айдишник = " + review.getReviewId());
        System.out.println("Сейчас будет возвращено ревью: " + review);
        return review;
    }

    @Override
    public Review updateReview(Review review) {
        boolean reply = jdbcTemplate.update(UPDATE_REVIEW,
                review.getContent(),
                review.isPositive(),
                review.getReviewId()) < 1;
        if (reply) {
            throw new ReviewNotFoundException("Ошибка при обновлении отзыва с ID = " + review.getReviewId() + ".");
        }
        return findReviewById(review.getReviewId()).get();
    }

    @Override
    public void deleteReview(long id) {
        if (jdbcTemplate.update(DELETE_REVIEW, id) < 1) {
            throw new ReviewNotFoundException("Ошибка при удалении отзыва с ID = " + id + ".");
        } else {
            log.info("Отзыв с ID = {} удален.", id);
        }
    }

    @Override
    public Optional<Review> findReviewById(long id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(FIND_BY_ID, (rs, rowNum) -> buildReview(rs), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Collection<Review> findReviewByCount(int count) {
        return jdbcTemplate.query(FIND_ALL_REVIEWS, (rs, rowNum) -> buildReview(rs), count);
    }

    @Override
    public Collection<Review> findReviewByIdFilm(Long filmId, int count) {
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

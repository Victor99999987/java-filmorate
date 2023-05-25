package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService service;

    @Autowired
    public ReviewController(ReviewService service) {
        this.service = service;
    }

    @PostMapping
    public Review createReview(@Valid @NotNull @RequestBody Review review) {
        return service.create(review);
    }

    @PutMapping
    public Review updateReview(@Valid @NotNull @RequestBody Review review) {
        return service.update(review);
    }

    @DeleteMapping("{id}")
    public Review deleteReview(@PathVariable long id) {
        return service.delete(id);
    }

    @GetMapping("{id}")
    public Review findById(@PathVariable long id) {
        return service.findById(id);
    }

    @GetMapping
    public Collection<Review> findAllByIdFilm(
            @RequestParam(required = false) Long filmId,
            @RequestParam(defaultValue = "10", required = false) int count) {
        return service.findReviewByIdFilm(filmId, count);
    }

    @PutMapping("{id}/like/{userId}")
    public Review addLike(@PathVariable long id, @PathVariable long userId) {
        return service.addLike(id, userId);
    }

    @PutMapping("{id}/dislike/{userId}")
    public Review addDislike(@PathVariable long id, @PathVariable long userId) {
        return service.addDislike(id, userId);
    }

    @DeleteMapping("{id}/like/{userId}")
    public Review removeLike(@PathVariable long id, @PathVariable long userId) {
        return service.removeLike(id, userId);
    }

    @DeleteMapping("{id}/dislike/{userId}")
    public Review removeDislike(@PathVariable long id, @PathVariable long userId) {
        return service.removeDislike(id, userId);
    }

}
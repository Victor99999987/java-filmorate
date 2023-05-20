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
    public Review create(@Valid @NotNull @RequestBody Review review) {
        Review createdReview = service.create(review);
        System.out.println("Controller says: review was created! " + createdReview);
        return createdReview;
    }

    @PutMapping
    public Review update(@Valid @NotNull @RequestBody Review review) {
        service.update(review);
        return review;
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable long id) {
        service.delete(id);
    }

    @GetMapping("{id}")
    public Review findById(@PathVariable long id) {
        return service.findById(id);
    }

    @GetMapping
    public Collection<Review> findAllBiIdFilm(
            @RequestParam(required = false) Long filmId,
            @RequestParam(defaultValue = "10", required = false) int count) {
        return service.findReviewByIdFilm(filmId, count);
    }

    @PutMapping("{id}/like/{userId}")
    public void like(@PathVariable long id, @PathVariable long userId) {
        service.addLike(id, userId);
    }

    @PutMapping("{id}/dislike/{userId}")
    public void dislike(@PathVariable long id, @PathVariable long userId) {
        service.addDislike(id, userId);
    }

    @DeleteMapping("{id}/like/{userId}")
    public void deleteLike(@PathVariable long id, @PathVariable long userId) {
        service.removeLike(id, userId);
    }

    @DeleteMapping("{id}/dislike/{userId}")
    public void deleteDislike(@PathVariable long id, @PathVariable long userId) {
        service.removeDislike(id, userId);
    }

}
package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/genres")
public class GenreController {
    private final GenreService genreService;

    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    @GetMapping
    public List<Genre> getAll() {
        return genreService.getAll();
    }

    @GetMapping("/{id}")
    public Genre getById(@PathVariable Long id) {
        return genreService.getById(id);
    }

    @PostMapping
    public Genre add(@NotNull @Valid @RequestBody Genre genre) {
        return genreService.add(genre);
    }

    @PutMapping
    public Genre update(@NotNull @Valid @RequestBody Genre genre) {
        return genreService.update(genre);
    }

    @DeleteMapping("/{id}")
    public Genre remove(@PathVariable Long id) {
        return genreService.remove(id);
    }

}

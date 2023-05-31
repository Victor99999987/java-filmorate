package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@RequestMapping("/directors")

public class DirectorController {
    private final DirectorService directorService;

    public DirectorController(DirectorService directorService) {
        this.directorService = directorService;
    }

    @GetMapping
    public Collection<Director> getAll() {
        return directorService.getAll();
    }

    @PostMapping
    public Director create(@Valid @RequestBody Director director) {
        return directorService.add(director);
    }

    @PutMapping
    public Director put(@Valid @RequestBody Director director) {
        return directorService.update(director);
    }

    @GetMapping("{id}")
    public Director getById(@PathVariable Long id) {
        return directorService.getById(id);
    }

    @DeleteMapping("{id}")
    public void remove(@PathVariable Long id) {
        directorService.remove(id);
    }
}
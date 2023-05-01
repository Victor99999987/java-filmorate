package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/mpa")
public class MpaController {
    private final MpaService mpaService;

    public MpaController(MpaService mpaService) {
        this.mpaService = mpaService;
    }

    @GetMapping
    public List<Mpa> getAll() {
        return mpaService.getAll();
    }

    @GetMapping("/{id}")
    public Mpa getById(@PathVariable Long id) {
        return mpaService.getById(id);
    }

    @PostMapping
    public Mpa add(@NotNull @Valid @RequestBody Mpa mpa) {
        return mpaService.add(mpa);
    }

    @PutMapping
    public Mpa update(@NotNull @Valid @RequestBody Mpa mpa) {
        return mpaService.update(mpa);
    }

    @DeleteMapping("/{id}")
    public Mpa remove(@PathVariable Long id) {
        return mpaService.remove(id);
    }

}

package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.util.List;

@Slf4j
@Service
public class MpaService {
    private final Storage<Mpa> mpaStorage;

    public MpaService(@Qualifier("DbMpaStorage") Storage<Mpa> mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    public List<Mpa> getAll() {
        return mpaStorage.getAll();
    }

    public Mpa getById(Long id) {
        return mpaStorage.getById(id);
    }

    public Mpa add(Mpa mpa) {
        return mpaStorage.add(mpa);
    }

    public Mpa remove(Long id) {
        return mpaStorage.remove(id);
    }

    public Mpa update(Mpa mpa) {
        return mpaStorage.update(mpa);
    }

}

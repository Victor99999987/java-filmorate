package ru.yandex.practicum.filmorate.storage.impl.inmemory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.MpaNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationMpaException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.Storage;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Qualifier("InMemoryMpaStorage")
public class InMemoryMpaStorage implements Storage<Mpa> {

    private final Map<Long, Mpa> mpas = new HashMap<>();
    private Long generateId = 0L;

    @Override
    public List<Mpa> getAll() {
        return new ArrayList<>(mpas.values());
    }

    @Override
    public Mpa getById(Long id) {
        if (!mpas.containsKey(id)) {
            log.info(String.format("MpaNotFoundException: Не найден mpa-рейтинг с id=%d", id));
            throw new MpaNotFoundException(String.format("Не найден mpa-рейтинг с id=%d", id));
        }
        return mpas.get(id);
    }

    @Override
    public Mpa add(Mpa mpa) {
        if (mpa.getId() != null) {
            log.info("ValidationMpaException: При добавлении mpa-рейтинга id должен быть null");
            throw new ValidationMpaException("При добавлении mpa-рейтинга id должен быть null");
        }
        mpa.setId(++generateId);
        mpas.put(mpa.getId(), mpa);
        return mpa;
    }

    @Override
    public Mpa remove(Long id) {
        if (!mpas.containsKey(id)) {
            log.info(String.format("MpaNotFoundException: При удалении не найден mpa-рейтинг с id=%d", id));
            throw new MpaNotFoundException(String.format("При удалении не найден mpa-рейтинг с id=%d", id));
        }
        Mpa mpa = mpas.get(id);
        mpas.remove(id);
        return mpa;
    }

    @Override
    public Mpa update(Mpa mpa) {
        if (!mpas.containsKey(mpa.getId())) {
            log.info(String.format("MpaNotFoundException: При обновлении не найден mpa-рейтинг с id=%d", mpa.getId()));
            throw new MpaNotFoundException(String.format("При обновлении не найден mpa-рейтинг с id=%d", mpa.getId()));
        }
        mpas.put(mpa.getId(), mpa);
        return mpa;
    }
}

package ru.yandex.practicum.filmorate.storage;

import java.util.List;

public interface Storage<T> {
    public List<T> getAll();

    public T getById(Long id);

    public T add(T genre);

    public T remove(Long id);

    public T update(T t);
}

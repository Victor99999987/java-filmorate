package ru.yandex.practicum.filmorate.storage.impl.db;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

@AllArgsConstructor
public abstract class DbStorage {
    protected final JdbcTemplate jdbcTemplate;
}

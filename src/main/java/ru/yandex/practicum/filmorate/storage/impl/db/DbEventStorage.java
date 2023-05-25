package ru.yandex.practicum.filmorate.storage.impl.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.EventNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationEventException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.type.EventType;
import ru.yandex.practicum.filmorate.model.type.OperationType;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Qualifier("DbEventStorage")
public class DbEventStorage extends DbStorage implements Storage<Event> {

    private final String selectSQL = "(" +
            "select e.id, e.timestmp, e.users_id, e.eventtype, e.operation, e.entitys_users_id as entitys_id  \n" +
            "from events as e\n" +
            "where e.entitys_users_id is not null and_condition\n" +
            "union\n" +
            "select e.id, e.timestmp, e.users_id, e.eventtype, e.operation, e.entitys_films_id as entitys_id  \n" +
            "from events as e\n" +
            "where e.entitys_films_id is not null and_condition\n" +
            "union \n" +
            "select e.id, e.timestmp, e.users_id, e.eventtype, e.operation, e.entitys_reviews_id as entitys_id  \n" +
            "from events as e\n" +
            "where e.entitys_reviews_id is not null and_condition\n" +
            ") order by timestmp desc; ";

    public DbEventStorage(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public List<Event> getAll() {
        List<Event> result = new ArrayList<>();
        String sql = selectSQL.replaceAll("and_condition", "");
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql);
        while (sqlRowSet.next()) {
            result.add(makeEvent(sqlRowSet));
        }
        return result;
    }

    @Override
    public Event getById(Long id) {
        String sql = selectSQL.replaceAll("and_condition", "and e.id = " + id);
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql);
        if (!sqlRowSet.first()) {
            log.info(String.format("EventNotFoundException: Не найдено событие с id=%d", id));
            throw new EventNotFoundException(String.format("Не найдено событие с id=%d", id));
        }
        return makeEvent(sqlRowSet);
    }

    @Override
    public Event add(Event event) {
        if (event.getEventId() != null) {
            log.info("ValidationEventException: При добавлении события id должен быть null");
            throw new ValidationEventException("При добавлении события id должен быть null");
        }
        String sql = "insert into events (timestmp, users_id, eventtype, operation, entitys_id) values(?, ?, ?, ?, ?)";
        switch (event.getEventType()){
            case LIKE:
                sql = sql.replace("entitys_id", "entitys_films_id");
                break;
            case FRIEND:
                sql = sql.replace("entitys_id", "entitys_users_id");
                break;
            case REVIEW:
                sql = sql.replace("entitys_id", "entitys_reviews_id");
                break;
        }
        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
        String finalSql = sql;
        jdbcTemplate.update(conn -> {
            PreparedStatement preparedStatement = conn.prepareStatement(finalSql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setLong(1, event.getTimestamp());
            preparedStatement.setLong(2, event.getUserId());
            preparedStatement.setString(3, event.getEventType().toString());
            preparedStatement.setString(4, event.getOperation().toString());
            preparedStatement.setLong(5, event.getEntityId());
            return preparedStatement;
        }, generatedKeyHolder);
        event.setEventId(generatedKeyHolder.getKey().longValue());
        return event;
    }

    @Override
    public Event remove(Long id) {
        Event event = getById(id);
        String sql = "delete from events where id = ?";
        jdbcTemplate.update(sql, id);
        return event;
    }

    @Override
    public Event update(Event event) {
        getById(event.getEventId());
        String sql = "update events set timestmp = ?, users_id = ?, eventtype = ?, operation = ?, entitys_id = ? where id = ?";
        switch (event.getEventType()){
            case LIKE:
                sql = sql.replace("entitys_id", "entitys_films_id");
                break;
            case FRIEND:
                sql = sql.replace("entitys_id", "entitys_users_id");
                break;
            case REVIEW:
                sql = sql.replace("entitys_id", "entitys_reviews_id");
                break;
        }
        jdbcTemplate.update(sql, event.getTimestamp(), event.getUserId(), event.getEventType(), event.getEntityId(),
                event.getOperation(), event.getEventId());
        return event;
    }

    private Event makeEvent(SqlRowSet rs) {
        return Event.builder()
                .eventId(rs.getLong("id"))
                .timestamp(rs.getLong("timestmp")) //.timestamp(Timestamp.valueOf(rs.getString("timestmp")))
                .userId(rs.getLong("users_id"))
                .eventType(EventType.valueOf(rs.getString("eventtype")))
                .operation(OperationType.valueOf(rs.getString("operation")))
                .entityId(rs.getLong(6))//"entitys_id"
                .build();
    }
}

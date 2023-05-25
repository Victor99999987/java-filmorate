package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.yandex.practicum.filmorate.model.type.EventType;
import ru.yandex.practicum.filmorate.model.type.OperationType;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@AllArgsConstructor
@Builder
public class Event {
    @EqualsAndHashCode.Include
    private Long eventId;
    @Positive
    @EqualsAndHashCode.Exclude
    private Long timestamp;
    @Positive
    @EqualsAndHashCode.Exclude
    private Long userId;
    @NotNull
    @EqualsAndHashCode.Exclude
    private EventType eventType;
    @NotNull
    @EqualsAndHashCode.Exclude
    private OperationType operation;
    @Positive
    @EqualsAndHashCode.Exclude
    private Long entityId;
}

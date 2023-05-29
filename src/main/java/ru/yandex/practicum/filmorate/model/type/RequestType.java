package ru.yandex.practicum.filmorate.model.type;

import ru.yandex.practicum.filmorate.exception.ValidationSortException;

public enum RequestType {
    NO_PARAM("noParam"),
    LIKES("likes"),
    YEAR("year");

    private final String typeValue;

    RequestType(String type) {
        typeValue = type;
    }

    public static RequestType getType(String pType) {
        for (RequestType type: RequestType.values()) {
            if (type.getTypeValue().equals(pType)) {
                return type;
            }
        }
        throw new ValidationSortException("Неверный параметр сортировки");
    }

    public String getTypeValue() {
        return typeValue;
    }
}

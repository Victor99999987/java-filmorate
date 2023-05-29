package ru.yandex.practicum.filmorate.model.type;

public enum RequestType {
    NO_PARAM("noParam"),
    LIKES("likes"),
    YEAR("year");

    private String typeValue;

    private RequestType(String type) {
        typeValue = type;
    }

    static public RequestType getType(String pType) {
        for (RequestType type: RequestType.values()) {
            if (type.getTypeValue().equals(pType)) {
                return type;
            }
        }
        throw new RuntimeException("unknown type");
    }

    public String getTypeValue() {
        return typeValue;
    }
}

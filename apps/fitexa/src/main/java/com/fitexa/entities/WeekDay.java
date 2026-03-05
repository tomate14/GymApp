package com.fitexa.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum WeekDay {
    LUNES("lunes"),
    MARTES("martes"),
    MIERCOLES("miercoles"),
    JUEVES("jueves"),
    VIERNES("viernes"),
    SABADO("sabado"),
    DOMINGO("domingo");

    private final String value;

    WeekDay(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static WeekDay fromValue(String value) {
        return Arrays.stream(values())
            .filter(item -> item.value.equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Dia invalido: " + value));
    }
}

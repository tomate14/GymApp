package com.fitexa.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum AdminPanelModule {
    DASHBOARD("dashboard", "Dashboard", "dashboard"),
    AGENDA("agenda", "Agenda", "agenda-clases"),
    USERS("users", "Usuarios", "usuarios"),
    PLANS("plans", "Planes", "planes"),
    ACTIVITIES("activities", "Actividades", "actividades"),
    CLASSES("classes", "Clases", "clases"),
    ROUTINES("routines", "Rutinas", "rutinas"),
    EXERCISES("exercises", "Ejercicios", "ejercicios"),
    MACHINES("machines", "Maquinas", "maquinas"),
    ROLES_PERMISSIONS("roles_permissions", "Roles y permisos", "roles-permisos");

    private final String value;
    private final String label;
    private final String path;

    AdminPanelModule(String value, String label, String path) {
        this.value = value;
        this.label = label;
        this.path = path;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public String getPath() {
        return path;
    }

    @JsonCreator
    public static AdminPanelModule fromValue(String value) {
        return Arrays.stream(values())
            .filter(item -> item.value.equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Modulo invalido: " + value));
    }
}

package com.fitexa.services;

public enum DashboardStudentsTrendMode {
    MONTH,
    YEAR;

    public static DashboardStudentsTrendMode fromValue(String value) {
        if (value == null || value.isBlank()) {
            return MONTH;
        }
        if ("year".equalsIgnoreCase(value) || "anio".equalsIgnoreCase(value) || "ano".equalsIgnoreCase(value)) {
            return YEAR;
        }
        return MONTH;
    }
}

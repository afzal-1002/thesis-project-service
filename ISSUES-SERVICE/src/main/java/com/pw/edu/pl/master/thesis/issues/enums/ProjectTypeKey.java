package com.pw.edu.pl.master.thesis.issues.enums;

public enum ProjectTypeKey {
    SOFTWARE("software"),
    PRODUCT_DISCOVERY("product_discovery"),
    SERVICE_DESK("service_desk");

    private final String key;

    ProjectTypeKey(String key) { this.key = key; }

    @Override
    public String toString() {
        return key;
    }

    public static ProjectTypeKey fromString(String key) {
        for (ProjectTypeKey type : ProjectTypeKey.values()) {
            if (type.key.equalsIgnoreCase(key)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown ProjectTypeKey: " + key);
    }
}
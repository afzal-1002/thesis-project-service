package com.pw.edu.pl.master.thesis.project.enums;

public enum Role {
    USER,
    ADMIN,
    TESTER,
    DEVELOPER;
    public static Role from(String string) { return Role.valueOf(string.trim().toUpperCase()); }
}

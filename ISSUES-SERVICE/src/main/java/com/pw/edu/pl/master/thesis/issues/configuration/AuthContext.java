package com.pw.edu.pl.master.thesis.issues.configuration;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class AuthContext {
    private AuthContext() {}
    public static String currentUsernameOrThrow() {
        Authentication user = SecurityContextHolder.getContext().getAuthentication();
        if (user == null || !user.isAuthenticated()) {
            throw new IllegalStateException("Unauthenticated request");
        }
        return user.getName();
    }
}
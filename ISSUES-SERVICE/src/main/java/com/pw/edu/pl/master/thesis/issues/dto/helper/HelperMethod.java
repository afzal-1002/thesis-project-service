package com.pw.edu.pl.master.thesis.issues.dto.helper;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class HelperMethod {

    public void requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    public  <T> void requireNonNull(T value, String message) {
        if (value == null) throw new IllegalArgumentException(message);
    }

    public void requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(message);
    }


    public boolean hasText(String string) {
        return string != null && !string.trim().isEmpty();
    }

    public String safe(String string) {
        return hasText(string) ? string.trim() : null;
    }


    private static String seg(String value) {
        return UriUtils.encodePathSegment(value, StandardCharsets.UTF_8);
    }

    public String tryExtractIssueKeyFromSelf(String selfUrl) {
        if (selfUrl == null) return null;

        String markerStart = "/issue/";
        String markerEnd = "/comment/";

        int startIndex = selfUrl.indexOf(markerStart);
        if (startIndex == -1) return null;

        startIndex += markerStart.length();
        int endIndex = selfUrl.indexOf(markerEnd, startIndex);

        if (endIndex == -1) return null;

        return selfUrl.substring(startIndex, endIndex);
    }



}

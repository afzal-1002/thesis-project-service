package com.pw.edu.pl.master.thesis.user.model.helper;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
@RequiredArgsConstructor
public class HelperMethod {


    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    public String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String hashedPassword) {
        return !passwordEncoder.matches(rawPassword, hashedPassword);
    }
    public String encrypt(String plaintext) {
        return passwordEncoder.encode(plaintext);
    }
    public String decrypt(String ciphertext) {
        return ciphertext;
    }



    public String getSiteName(String jiraUrl) {
        if (jiraUrl == null || jiraUrl.isBlank()) {
            throw new IllegalArgumentException("Jira URL cannot be null or empty");
        }
        try {
            URI uri = URI.create(jiraUrl.trim());
            String host = uri.getHost();
            if (host == null || !host.endsWith(".atlassian.net")) {
                throw new IllegalArgumentException("Invalid Jira Cloud URL: " + jiraUrl);
            }
            return host.substring(0, host.indexOf(".atlassian.net"));
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not parse Jira URL: " + jiraUrl, e);
        }
    }

    // NonBlank Check
    public void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty");
        }
    }


}

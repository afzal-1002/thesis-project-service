package com.pw.edu.pl.master.thesis.issues.configuration;


import com.pw.edu.pl.master.thesis.issues.exception.ResourceNotFoundException;
import com.pw.edu.pl.master.thesis.issues.exception.UserNotAuthorizedException;
import com.pw.edu.pl.master.thesis.issues.dto.helper.EncryptionDecryption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class JiraClientConfiguration {

    private final RestTemplate restTemplate;
    private final EncryptionDecryption encryptionService;

    public <T> T get(String url, Class<T> responseType, String username, String token) {
        return exchange(url, HttpMethod.GET, null, responseType, username, token);
    }

    public <T> T post(String url, Object payload, Class<T> responseType, String username, String token) {
        return exchange(url, HttpMethod.POST, payload, responseType, username, token);
    }

    public <T> T put(String url, Object payload, Class<T> responseType, String username, String token) {
        return exchange(url, HttpMethod.PUT, payload, responseType, username, token);
    }

    public <T> T patch(String url, Object payload, Class<T> responseType, String username, String token) {
        return exchange(url, HttpMethod.PATCH, payload, responseType, username, token);
    }

    public <T> T delete(String url, Class<T> responseType, String username, String token) {
        return exchange(url, HttpMethod.DELETE, null, responseType, username, token);
    }


    // ─────────────────────────── Core Logic ───────────────────────────

    private <T> T exchange(String url, HttpMethod method, Object payload, Class<T> responseType, String username, String token) {
        if (isBlank(username) || isBlank(token)) {
            throw new UserNotAuthorizedException("Missing Jira credentials (username or token).");
        }

        // Decrypt token only if stored encrypted
        String decryptedToken = encryptionService.decrypt(token);

        HttpHeaders headers = buildHeaders(username, decryptedToken, payload != null);
        HttpEntity<?> entity = (payload == null)
                ? new HttpEntity<>(headers)
                : new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<T> response = restTemplate.exchange(url, method, entity, responseType);
            ensureJsonResponse(response, responseType);
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            throw mapHttpError(e);
        }
    }

    // ─────────────────────────── Helper Methods ───────────────────────────

    private HttpHeaders buildHeaders(String username, String token, boolean hasBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (hasBody) headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(username, token, StandardCharsets.UTF_8);
        return headers;
    }

    private static void ensureJsonResponse(ResponseEntity<?> response, Class<?> responseType) {
        if (Void.class.equals(responseType)) return;
        MediaType contentType = response.getHeaders().getContentType();
        if (contentType == null || !MediaType.APPLICATION_JSON.isCompatibleWith(contentType)) {
            throw new IllegalStateException("Expected JSON response but got: " + contentType);
        }
    }

    private static RuntimeException mapHttpError(HttpStatusCodeException e) {
        HttpStatusCode status = e.getStatusCode();
        String msg = "Jira request failed: " + status.value() + " " + status + " " +
                Objects.toString(e.getResponseBodyAsString(), "");
        if (status == HttpStatus.UNAUTHORIZED || status == HttpStatus.FORBIDDEN) {
            return new UserNotAuthorizedException(msg);
        } else if (status == HttpStatus.NOT_FOUND) {
            return new ResourceNotFoundException(msg);
        } else {
            return new IllegalStateException(msg, e);
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }


}

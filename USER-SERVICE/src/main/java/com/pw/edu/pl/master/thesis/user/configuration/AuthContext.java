package com.pw.edu.pl.master.thesis.user.configuration;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class AuthContext {

    private final HttpServletRequest request;

    /** Username from SecurityContext (Basic auth principal). */
    public String currentUsernameOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            throw new IllegalStateException("No authenticated user in SecurityContext");
        }
        return auth.getName();
    }

    /**
     * Best-effort current password.
     * 1) from Authentication.getCredentials() if eraseCredentials=false
     * 2) decode from Authorization header if present
     */
    public String currentPasswordOrNull() {
        // 1) Try SecurityContext credentials
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getCredentials() instanceof String creds && !creds.isBlank()) {
            return creds;
        }

        // 2) Fallback: decode Basic header
        String header = request.getHeader("Authorization");
        if (header != null && header.regionMatches(true, 0, "Basic ", 0, 6)) {
            try {
                String b64 = header.substring(6).trim();
                String decoded = new String(Base64.getDecoder().decode(b64), StandardCharsets.UTF_8);
                int idx = decoded.indexOf(':');
                if (idx > 0) {
                    return decoded.substring(idx + 1);
                }
            } catch (Exception ignored) { }
        }
        return null;
    }

    /** Password but throws if not available (useful if you require it). */
    public String currentPasswordOrThrow() {
        String pwd = currentPasswordOrNull();
        if (pwd == null || pwd.isBlank()) {
            throw new IllegalStateException("Cannot resolve Basic-Auth password for current request");
        }
        return pwd;
    }
}

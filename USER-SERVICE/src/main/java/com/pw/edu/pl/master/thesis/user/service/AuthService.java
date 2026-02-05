package com.pw.edu.pl.master.thesis.user.service;

import com.pw.edu.pl.master.thesis.user.dto.user.VerifyResponse;
import com.pw.edu.pl.master.thesis.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepo;
    private final EncryptionService encryptionService; // BCrypt matches

    public VerifyResponse verifyBasic(String basicHeader) {
        if (basicHeader == null || !basicHeader.startsWith("Basic "))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Basic auth");

        String decoded = new String(Base64.getDecoder()
                .decode(basicHeader.substring("Basic ".length())), StandardCharsets.UTF_8);
        int idx = decoded.indexOf(':');
        if (idx < 1) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad Basic format");
        String username = decoded.substring(0, idx);
        String password = decoded.substring(idx + 1);

        var user = userRepo.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (encryptionService.matches(password, user.getPassword()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");

        String accountId = user.getAccountId();
        String baseUrl   = (user.getUserCredential() != null) ? user.getUserCredential().getBaseUrl() : null;

        var roles = (user.getRoles() == null) ? List.<String>of()
                : user.getRoles().stream().map(Enum::name).toList();

        return new VerifyResponse(user.getId(), user.getUsername(), roles, accountId, baseUrl);
    }
}


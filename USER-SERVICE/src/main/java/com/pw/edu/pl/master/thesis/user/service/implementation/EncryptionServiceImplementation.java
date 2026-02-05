package com.pw.edu.pl.master.thesis.user.service.implementation;

import com.pw.edu.pl.master.thesis.user.dto.user.TokenResponse;
import com.pw.edu.pl.master.thesis.user.service.EncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EncryptionServiceImplementation implements EncryptionService {

    private final TextEncryptor textEncryptor;
    private final PasswordEncoder passwordEncoder;

    @Override
    public String hashPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("rawPassword is required");
        }
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String hashedPassword) {
        if (rawPassword == null || hashedPassword == null) return false;
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }

    @Override
    public TokenResponse encrypt(String plaintext) {
        if (plaintext == null || plaintext.isBlank()) {
            throw new IllegalArgumentException("plaintext cannot be empty");
        }
//        return TokenResponse.builder().result(textEncryptor.encrypt(plaintext)).build();
        return TokenResponse.builder().result((plaintext)).build();

    }

    @Override
    public TokenResponse decrypt(String ciphertext) {
        if (ciphertext == null || ciphertext.isBlank()) {
            throw new IllegalArgumentException("encryptedToken is required");
        }
        try {
//            return TokenResponse.builder().result(textEncryptor.decrypt(ciphertext)).build();
            return TokenResponse.builder().result(ciphertext).build();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to decrypt token. Check crypto.password / crypto.salt.", ex);
        }
    }
}

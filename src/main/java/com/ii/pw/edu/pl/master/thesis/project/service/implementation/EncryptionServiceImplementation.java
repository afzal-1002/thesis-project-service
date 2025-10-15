package com.ii.pw.edu.pl.master.thesis.project.service.implementation;

import com.ii.pw.edu.pl.master.thesis.project.service.EncryptionService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class EncryptionServiceImplementation implements EncryptionService {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String hashedPassword) {
        return !passwordEncoder.matches(rawPassword, hashedPassword);
    }

    @Override
    public String encrypt(String plaintext) {
        return plaintext;
    }

    @Override
    public String decrypt(String ciphertext) {
        return ciphertext;
    }
}

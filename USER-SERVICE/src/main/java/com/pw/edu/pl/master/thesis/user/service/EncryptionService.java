package com.pw.edu.pl.master.thesis.user.service;

import com.pw.edu.pl.master.thesis.user.dto.user.TokenResponse;
import org.springframework.stereotype.Service;

@Service
public interface EncryptionService {

    String hashPassword(String rawPassword);
    boolean matches(String rawPassword, String hashedPassword);
    TokenResponse encrypt(String plaintext);
    TokenResponse decrypt(String ciphertext);

}

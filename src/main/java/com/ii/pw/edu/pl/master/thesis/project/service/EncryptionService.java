package com.ii.pw.edu.pl.master.thesis.project.service;

import org.springframework.stereotype.Service;

@Service
public interface EncryptionService {
    String hashPassword(String rawPassword);
    boolean matches(String rawPassword, String hashedPassword);
    String encrypt(String plaintext);
    String decrypt(String ciphertext);

}

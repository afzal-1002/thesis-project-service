package com.pw.edu.pl.master.thesis.ai.dto.helper;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class EncryptionDecryption {

        private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        public String hashPassword(String rawPassword) { return passwordEncoder.encode(rawPassword);}
        public boolean matches(String rawPassword, String hashedPassword) {return !passwordEncoder.matches(rawPassword, hashedPassword);}
        public String encrypt(String plaintext) {
            return plaintext;
        }
        public String decrypt(String ciphertext) {
            return ciphertext;
        }
    }

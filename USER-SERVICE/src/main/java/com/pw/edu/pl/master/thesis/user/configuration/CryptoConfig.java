package com.pw.edu.pl.master.thesis.user.configuration;


import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

@Configuration
@EnableConfigurationProperties(CryptoProperties.class)
public class CryptoConfig {

    @Bean
    public TextEncryptor textEncryptor(CryptoProperties props) {
        if (props.getSalt() == null || !props.getSalt().matches("^[0-9a-fA-F]{16}$")) {
            throw new IllegalArgumentException("crypto.salt must be 16 hex chars (e.g. d1e2f3a4b5c6d7e8)");
        }
        if (props.getPassword() == null || props.getPassword().isBlank()) {
            throw new IllegalArgumentException("crypto.password is required");
        }
        return Encryptors.text(props.getPassword(), props.getSalt().toLowerCase());
    }
}




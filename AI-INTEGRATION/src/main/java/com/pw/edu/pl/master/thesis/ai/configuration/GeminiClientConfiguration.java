package com.pw.edu.pl.master.thesis.ai.configuration;

import com.google.genai.Client;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class GeminiClientConfiguration {

    // The API key retrieved from application.properties
    @Value("${gemini.api.key}")
    private String apiKey ;

    @Bean
    public Client geminiClient() {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Gemini API Key is not configured. Please set the 'gemini.api.key' property in application.properties.");
        }

        log.info("Initializing Gemini Client with provided API Key.");
        return Client.builder()
                .apiKey(apiKey)
                .build();
    }

}

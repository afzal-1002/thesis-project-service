package com.pw.edu.pl.master.thesis.ai.dto.ai.deepseek;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "spring.ai.deepseek")
public class DeepSeekProperties {
    private String baseUrl;
    private String apiKey;
}
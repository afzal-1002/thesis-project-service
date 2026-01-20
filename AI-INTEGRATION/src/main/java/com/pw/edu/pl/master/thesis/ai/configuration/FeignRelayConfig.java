package com.pw.edu.pl.master.thesis.ai.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignRelayConfig {
    @Bean
    feign.RequestInterceptor relayBasicHeader() {
        return template -> {
            var attrs = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (attrs instanceof org.springframework.web.context.request.ServletRequestAttributes sra) {
                String auth = sra.getRequest().getHeader("Authorization");
                if (auth != null) template.header("Authorization", auth);
            }
        };
    }
}


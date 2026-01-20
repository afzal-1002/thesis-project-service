package com.pw.edu.pl.master.thesis.issues.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Configuration
public class FeignRelayConfig {
    @Bean
    feign.RequestInterceptor relayBasicHeader() {
        return template -> {
            var attrs = RequestContextHolder.getRequestAttributes();
            if (attrs instanceof ServletRequestAttributes sra) {
                String auth = sra.getRequest().getHeader("Authorization");
                if (auth != null) template.header("Authorization", auth);
            }
        };
    }
}


package com.pw.edu.pl.master.thesis.project.configuration;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignSecurityConfiguration {

    @Bean
    public RequestInterceptor basicAuthRelayInterceptor() {
        return template -> {
            RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
            if (attrs instanceof ServletRequestAttributes sra) {
                HttpServletRequest req = sra.getRequest();
                String auth = req.getHeader("Authorization");
                if (auth != null && auth.startsWith("Basic ")) {
                    template.header("Authorization", auth);
                }
            }
        };
    }
}
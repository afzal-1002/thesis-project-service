//package com.ii.pw.edu.pl.master.thesis.project.configuration;
//
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.CorsConfigurationSource;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//
//import java.util.List;
//
//@Configuration
//public class CorsConfig {
//
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration cors = new CorsConfiguration();
//        cors.setAllowedOrigins(List.of("http://localhost:4200"));
//        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//        cors.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
//        cors.setAllowCredentials(true);
//        cors.setMaxAge(3600L);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", cors);
//        return source;
//    }
//}

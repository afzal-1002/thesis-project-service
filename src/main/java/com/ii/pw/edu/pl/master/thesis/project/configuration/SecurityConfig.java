package com.ii.pw.edu.pl.master.thesis.project.configuration;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Password encoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Temporary in-memory users for testing protected endpoints
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        UserDetails admin = User.withUsername("admin")
                .password(encoder.encode("123456"))
                .roles("ADMIN")
                .build();
        UserDetails user = User.withUsername("user")
                .password(encoder.encode("user123"))
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(admin, user);
    }

    // CORS — permissive for localhost:4200 (Angular)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Use setAllowedOriginPatterns when you later need wildcards
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowCredentials(true);
        config.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*")); // allow any header Angular may send
        config.setExposedHeaders(Arrays.asList("Authorization","Set-Cookie")); // optional
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                .cors(c -> c.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth
                        // CORS preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Public auth endpoints
                        .requestMatchers(HttpMethod.POST,
                                "/api/wut/users/register",
                                "/api/wut/users/login",
                                "/api/wut/users/logout"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/wut/users").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/wut/credentials/me").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/wut/credentials/me/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/wut/users/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/wut/users/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/wut/projects/users").permitAll()



                        // Public READ-ONLY Site endpoints
                        .requestMatchers(HttpMethod.GET, "/api/wut/sites/**").permitAll()

                        // Swagger & actuator
                        .requestMatchers(
                                "/actuator/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // Everything else secured
                        .anyRequest().authenticated()
                )

                // TEMP: enable HTTP Basic for quick testing of secured endpoints
                .httpBasic(Customizer.withDefaults())
                // Disable form login for API style (keep basic on while testing)
                .formLogin(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
}

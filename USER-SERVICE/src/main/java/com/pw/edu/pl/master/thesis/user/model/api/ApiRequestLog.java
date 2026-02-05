package com.pw.edu.pl.master.thesis.user.model.api;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "api_request_logs")
public class ApiRequestLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String method;

    private String url;

    private Integer statusCode;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private LocalDateTime requestTime;

    private Long responseTimeMs;

    private String userIdentifier;

    @Column(columnDefinition = "TEXT")
    private String requestBody;

    private String frontendApp;

    private Boolean success;
}

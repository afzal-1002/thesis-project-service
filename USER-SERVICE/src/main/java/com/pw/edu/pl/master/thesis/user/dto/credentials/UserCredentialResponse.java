package com.pw.edu.pl.master.thesis.user.dto.credentials;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class UserCredentialResponse {
    private Long userId;
    private String username;
    private OffsetDateTime createdAt;
    private OffsetDateTime expiresAt;
    private String accountId;
    private String token;
    private String baseUrl;
}
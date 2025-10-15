package com.ii.pw.edu.pl.master.thesis.project.dto.credentials;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class UserCredentialResponse {
    private Long userId;
    private String jiraUsername;
    private String accountId;
    private String token;
    private String jiraBaseUrl;

    private OffsetDateTime createdAt;
    private OffsetDateTime expiresAt;
}
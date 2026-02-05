package com.pw.edu.pl.master.thesis.project.dto.credentials;

import lombok.*;

import java.time.OffsetDateTime;


@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor @Builder
public class UserCredentialRequest {
    private String username;
    private String accountId;
    private String token;
    private String jiraBaseUrl;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
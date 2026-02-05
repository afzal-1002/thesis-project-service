package com.pw.edu.pl.master.thesis.user.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String accountId;
    private String displayName;
    private String emailAddress;
    private String username;
    private int sessionTimeout;
    private boolean isActive;
}
package com.pw.edu.pl.master.thesis.issues.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
    private String userName;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private String displayName;
    private Boolean isActive;
    private String password;
}
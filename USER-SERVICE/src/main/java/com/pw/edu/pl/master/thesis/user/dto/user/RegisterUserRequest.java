package com.pw.edu.pl.master.thesis.user.dto.user;


import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterUserRequest {
    private String baserUrl;
    private String username;
    private String jiraToken;

    private String firstName;
    private String lastName;
    private String emailAddress;
    private String phoneNumber;
    private String password;

    private List<String> roles;

}

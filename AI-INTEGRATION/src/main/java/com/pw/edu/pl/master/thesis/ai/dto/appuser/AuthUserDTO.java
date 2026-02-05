package com.pw.edu.pl.master.thesis.ai.dto.appuser;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthUserDTO {
    private String username;
    private String password;
    private String roles;
}

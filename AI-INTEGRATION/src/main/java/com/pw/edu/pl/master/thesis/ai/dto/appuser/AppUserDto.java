package com.pw.edu.pl.master.thesis.ai.dto.appuser;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUserDto {
    private Long id;
    private String username;
    private String password;
    private String roles;
}
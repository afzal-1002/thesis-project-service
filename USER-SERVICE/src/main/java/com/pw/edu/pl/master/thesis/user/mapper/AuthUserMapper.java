package com.pw.edu.pl.master.thesis.user.mapper;

import com.pw.edu.pl.master.thesis.user.dto.appuser.AuthUserDTO;
import com.pw.edu.pl.master.thesis.user.model.user.AppUser;

public class AuthUserMapper {
    public static AuthUserDTO toAuthUserDTO(AppUser u) {
        return AuthUserDTO.builder()
                .username(u.getUsername())
                .password(u.getPassword())
                .roles(u.getRoles())
                .build();
    }
}
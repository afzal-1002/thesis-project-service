package com.pw.edu.pl.master.thesis.project.configuration;

import com.pw.edu.pl.master.thesis.project.client.ProfileClient;
import com.pw.edu.pl.master.thesis.project.dto.appuser.AuthUserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final ProfileClient profileClient;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AuthUserDTO authUser = profileClient.getByUsername(username);
        var authorities = Arrays.stream(authUser.getRoles().split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .toList();

        return new User(authUser.getUsername(), authUser.getPassword(), authorities);
    }

}

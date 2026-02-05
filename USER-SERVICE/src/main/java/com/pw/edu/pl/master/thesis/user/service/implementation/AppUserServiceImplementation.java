package com.pw.edu.pl.master.thesis.user.service.implementation;

import com.pw.edu.pl.master.thesis.user.dto.appuser.AuthUserDTO;
import com.pw.edu.pl.master.thesis.user.exception.BadCredentialsException;
import com.pw.edu.pl.master.thesis.user.mapper.AuthUserMapper;
import com.pw.edu.pl.master.thesis.user.model.user.AppUser;
import com.pw.edu.pl.master.thesis.user.repository.AppUserRepository;
import com.pw.edu.pl.master.thesis.user.service.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AppUserServiceImplementation implements AppUserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AppUser register(String username, String rawPassword, String roles) {
        appUserRepository.findByUsername(username).ifPresent(u -> {
            throw new IllegalStateException("Username already exists: " + username);
        });

        AppUser saved = AppUser.builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword)) // BCrypt!
                .roles((roles == null || roles.isBlank()) ? "USER" : roles)
                .build();

        return appUserRepository.save(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AppUser> findByUsername(String username) {
        return appUserRepository.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthUserDTO getByUsername(String username) {
        Optional<AppUser> appUser = appUserRepository.findByUsername(username);
        return appUser.map(AuthUserMapper::toAuthUserDTO).orElse(null);
    }


    @Override
    @Transactional(readOnly = true)
    public boolean validatePassword(String username, String rawPassword) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElse(null);
        if (user == null) return false;
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    @Override
    public String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return auth.getName(); // comes from Basic auth
    }

    @Override
    @Transactional(readOnly = true)
    public AppUser getCurrentUserOrThrow() {
        String username = getCurrentUsername();
        if (username == null) {
            throw new IllegalStateException("No authenticated user in context");
        }
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + username));
    }

    @Override
    public void assertPasswordMatches(String username, String rawPassword) {
        var appUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("AppUser not found"));
        if (!passwordEncoder.matches(rawPassword, appUser.getPassword())) {
            throw new BadCredentialsException("Basic password mismatch");
        }
    }

}

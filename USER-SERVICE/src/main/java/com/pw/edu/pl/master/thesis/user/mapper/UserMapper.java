package com.pw.edu.pl.master.thesis.user.mapper;


import com.pw.edu.pl.master.thesis.user.dto.user.LoginResponse;
import com.pw.edu.pl.master.thesis.user.enums.Role;
import com.pw.edu.pl.master.thesis.user.model.user.UserCredential;
import com.pw.edu.pl.master.thesis.user.model.user.User;
import com.pw.edu.pl.master.thesis.user.dto.user.UserSummary;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class UserMapper {

    // ---------- Entity → UserSummary ----------
    public UserSummary toUserSummary(User user) {
        if (user == null) return null;

        UserCredential cred = user.getUserCredential();

        UserSummary userSummary = new UserSummary();

        userSummary.setBaseUrl(cred != null ? cred.getBaseUrl() : null);
        userSummary.setAccountId(cred != null ? cred.getAccountId() : user.getAccountId());

        // User-sourced
        userSummary.setId(user.getId());
        userSummary.setFirstName(user.getFirstName());
        userSummary.setLastName(user.getLastName());
        userSummary.setEmailAddress(user.getEmailAddress());
        userSummary.setPhoneNumber(user.getPhoneNumber());
        userSummary.setActive(user.isActive());

        // Set<Role> -> List<Role>
        userSummary.setRoles(toRoleList(user.getRoles()));

        // Derived/optional fields (were ignored in MapStruct)
        if (user.getDisplayName() != null && !user.getDisplayName().isBlank()) {
            userSummary.setDisplayName(user.getDisplayName());
        } else {
            userSummary.setDisplayName(buildDisplayName(user.getFirstName(), user.getLastName(), user.getUsername()));
        }
        userSummary.setSelf(null);
        userSummary.setTimeZone(null);
        userSummary.setAvatarUrls(null);

        return userSummary;
    }

    // ---------- Entity → LoginResponse ----------
    public LoginResponse toLoginResponse(User user, int sessionTimeout) {
        if (user == null) return null;
        return LoginResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .emailAddress(user.getEmailAddress())
                .username(user.getUsername())
                .isActive(user.isActive())
                .sessionTimeout(sessionTimeout)
                .build();
    }

    // ---------- DTO → Entity ----------
    public User fromUserSummary(UserSummary summary) {
        if (summary == null) return null;

        User user = new User();
        user.setId(summary.getId());
        user.setUsername(nvl(summary.getUsername()));
        user.setFirstName(nvl(summary.getFirstName()));
        user.setLastName(nvl(summary.getLastName()));
        user.setEmailAddress(nvl(summary.getEmailAddress()));
        user.setPhoneNumber(summary.getPhoneNumber());
        if (summary.getActive() != null) {
            user.setActive(summary.getActive());
        }
        user.setRoles(toRoleSet(summary.getRoles()));
        // NOTE: password, jiraAccountId, etc. aren’t in UserSummary – set elsewhere
        user.setDisplayName(summary.getDisplayName());
        return user;
    }

    // ---------- helpers ----------
    private List<Role> toRoleList(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) return List.of();
        // keep insertion order if any
        return new ArrayList<>(new LinkedHashSet<>(roles));
    }

    private Set<Role> toRoleSet(List<Role> roles) {
        if (roles == null || roles.isEmpty()) return new LinkedHashSet<>();
        return new LinkedHashSet<>(roles);
    }

    private String buildDisplayName(String first, String last, String fallback) {
        String f = first == null ? "" : first.trim();
        String l = last == null ? "" : last.trim();
        String full = (f + " " + l).trim();
        return full.isEmpty() ? nvl(fallback) : full;
    }

    private String nvl(String s) { return s == null ? "" : s; }
}

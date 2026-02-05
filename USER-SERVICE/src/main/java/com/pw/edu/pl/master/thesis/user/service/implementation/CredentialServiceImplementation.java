package com.pw.edu.pl.master.thesis.user.service.implementation;

import com.pw.edu.pl.master.thesis.user.dto.credentials.UserCredentialRequest;
import com.pw.edu.pl.master.thesis.user.dto.credentials.UserCredentialResponse;
import com.pw.edu.pl.master.thesis.user.dto.user.TokenRequest;
import com.pw.edu.pl.master.thesis.user.dto.user.TokenResponse;
import com.pw.edu.pl.master.thesis.user.dto.user.UserSummary;
import com.pw.edu.pl.master.thesis.user.exception.ResourceNotFoundException;
import com.pw.edu.pl.master.thesis.user.mapper.UserMapper;
import com.pw.edu.pl.master.thesis.user.model.helper.JiraUrlBuilder;
import com.pw.edu.pl.master.thesis.user.model.user.AppUser;
import com.pw.edu.pl.master.thesis.user.model.user.User;
import com.pw.edu.pl.master.thesis.user.model.user.UserCredential;
import com.pw.edu.pl.master.thesis.user.repository.AppUserRepository;
import com.pw.edu.pl.master.thesis.user.repository.UserCredentialRepository;
import com.pw.edu.pl.master.thesis.user.repository.UserRepository;
import com.pw.edu.pl.master.thesis.user.service.AppUserService;
import com.pw.edu.pl.master.thesis.user.service.CredentialService;
import com.pw.edu.pl.master.thesis.user.service.EncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class CredentialServiceImplementation implements CredentialService {

    private final UserCredentialRepository userCredentialRepository;
    private final JiraUrlBuilder jiraUrlBuilder;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final EncryptionService encryptionService;
    private final AppUserService appUserService;
    private final AppUserRepository appUserRepository;

    // ─────────────────────────────────────────────
    // Create
    // ─────────────────────────────────────────────
    @Override
    @Transactional
    public UserCredentialResponse addCredential(UserCredentialRequest credentialRequest) {
        if (credentialRequest == null) throw new IllegalArgumentException("Credential cannot be null");
        if (credentialRequest.getUsername() == null || credentialRequest.getUsername().isBlank())
            throw new IllegalArgumentException("Username is required");
        if (credentialRequest.getToken() == null || credentialRequest.getToken().isBlank())
            throw new IllegalArgumentException("Jira token is required");
        if (credentialRequest.getBaseUrl() == null || credentialRequest.getBaseUrl().isBlank())
            throw new IllegalArgumentException("Jira base URL is required");

        User user = userRepository.findByUsername(credentialRequest.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String normalizedBase = jiraUrlBuilder.normalizeJiraBaseUrl(credentialRequest.getBaseUrl());
        String encryptedToken = encryptionService.encrypt(credentialRequest.getToken()).getResult();

        UserCredential credential = UserCredential.builder()
                .username(credentialRequest.getUsername())
                .accountId(credentialRequest.getAccountId())
                .baseUrl(normalizedBase)
                .token(encryptedToken) // store encrypted
                .user(user)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        UserCredential saved = userCredentialRepository.saveAndFlush(credential);
        return toCredentialsResponse(saved);
    }

    // ─────────────────────────────────────────────
    // Read
    // ─────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public UserCredential getUserCredential(String username) {
        return userCredentialRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Credential not found for username: " + username));
    }

    @Override
    @Transactional(readOnly = true)
    public String findAccountIdByUsername(String username) {
        return userCredentialRepository.findByUsername(username)
                .map(UserCredential::getAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Credential not found for username: " + username));
    }

    @Override
    @Transactional(readOnly = true)
    public UserCredentialResponse getCredentialByUserName(String username) {
        return toCredentialsResponse(getUserCredential(username));
    }

    @Override
    @Transactional(readOnly = true)
    public UserCredentialResponse findByCredentialId(Long credentialId) {
        UserCredential credential = userCredentialRepository.findById(credentialId)
                .orElseThrow(() -> new ResourceNotFoundException("Credential not found with id: " + credentialId));
        return toCredentialsResponse(credential);
    }

    /**
     * Secured: update the CURRENT caller’s base URL.
     * 'username' param is ignored; we take the principal from SecurityContext.
     */
    @Override
    @Transactional
    public UserCredentialResponse updateBaseUrlForCurrentUser(String ignoredUsername, String oldUrl, String newUrl) {
        if (oldUrl == null || oldUrl.isBlank() || newUrl == null || newUrl.isBlank()) {
            throw new IllegalArgumentException("Both oldUrl and newUrl are required");
        }

        String currentUsername = appUserService.getCurrentUsername();
        if (currentUsername == null) throw new IllegalStateException("Unauthenticated request");

        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUsername));

        UserCredential cred = userCredentialRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Credential not found for user: " + currentUsername));

        String oldNorm = jiraUrlBuilder.normalizeJiraBaseUrl(oldUrl);
        String newNorm = jiraUrlBuilder.normalizeJiraBaseUrl(newUrl);

        if (!oldNorm.equalsIgnoreCase(jiraUrlBuilder.normalizeJiraBaseUrl(cred.getBaseUrl()))) {
            throw new IllegalArgumentException("Old URL does not match current credential URL");
        }

        userCredentialRepository.findByBaseUrlIgnoreCase(newNorm).ifPresent(other -> {
            if (!other.getId().equals(cred.getId())) {
                throw new IllegalArgumentException("Another credential already uses URL: " + newNorm);
            }
        });

        cred.setBaseUrl(newNorm);
        UserCredential saved = userCredentialRepository.saveAndFlush(cred);
        return toCredentialsResponse(saved);
    }

    /**
     * Secured: update CURRENT caller’s token (ignore username param).
     */
    @Override
    @Transactional
    public UserCredentialResponse updateCredentialToken(String ignoredUsername, String newPlainToken) {
        if (newPlainToken == null || newPlainToken.isBlank()) {
            throw new IllegalArgumentException("New token is required");
        }

        String currentUsername = appUserService.getCurrentUsername();
        if (currentUsername == null) throw new IllegalStateException("Unauthenticated request");

        UserCredential cred = userCredentialRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Credential not found for username: " + currentUsername));

        String encrypted = encryptionService.encrypt(newPlainToken).getResult();

        if (encrypted.equals(cred.getToken())) {
            return toCredentialsResponse(cred); // no change
        }

        cred.setToken(encrypted);
        UserCredential saved = userCredentialRepository.saveAndFlush(cred);
        return toCredentialsResponse(saved);
    }

    // ─────────────────────────────────────────────
    // Delete
    // ─────────────────────────────────────────────
    @Override
    @Transactional
    public UserCredentialResponse deleteCredential(String username) {
        UserCredential credential = userCredentialRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Credential not found for username: " + username));
        UserCredentialResponse response = toCredentialsResponse(credential);
        userCredentialRepository.delete(credential);
        return response;
    }

    // ─────────────────────────────────────────────
    // Existence / crypto helpers
    // ─────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public boolean existsByJiraUsername(String username) {
        return userCredentialRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByAccountId(String accountId) {
        return userCredentialRepository.existsByAccountId(accountId);
    }

    @Override
    public TokenResponse encryptToken(TokenRequest tokenRequest) {
        if (tokenRequest == null || tokenRequest.getPlainToken() == null || tokenRequest.getPlainToken().isBlank()) {
            throw new IllegalArgumentException("Plain token is empty");
        }
        return encryptionService.encrypt(tokenRequest.getPlainToken());
    }

    @Override
    public TokenResponse decryptToken(TokenRequest cipherTokenRequest) {
        if (cipherTokenRequest == null || cipherTokenRequest.getEncryptedToken() == null
                || cipherTokenRequest.getEncryptedToken().isBlank()) {
            throw new IllegalArgumentException("Encrypted token is empty");
        }
        return encryptionService.decrypt(cipherTokenRequest.getEncryptedToken());
    }

    @Override
    @Transactional(readOnly = true)
    public UserSummary findByAccountId(String accountId) {
        UserCredential userCredential = userCredentialRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        User user = userRepository.findByUsername(userCredential.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toUserSummary(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserCredential getForCurrentUserOrThrow() {
        String username = appUserService.getCurrentUsername();
        if (username == null) throw new IllegalStateException("Unauthenticated request");
        return getByUsernameOrThrow(username);
    }

    @Override
    @Transactional(readOnly = true)
    public UserCredential getByUsernameOrThrow(String username) {
        AppUser appUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));

        return userCredentialRepository.findByUsername(appUser.getUsername())
                .orElseThrow(() -> new IllegalStateException("Jira credentials not configured for user: " + username));
    }

    @Override
    @Transactional
    public UserCredential addCredentialAndLinkToUsers(UserCredentialRequest req, User user, AppUser appUser) {
        String encrypted = encryptionService.encrypt(req.getToken()).getResult();

        UserCredential cred = UserCredential.builder()
                .username(req.getUsername())
                .accountId(req.getAccountId())
                .token(encrypted)
                .baseUrl(jiraUrlBuilder.normalizeJiraBaseUrl(req.getBaseUrl()))
                .user(user)       // FK to domain User
                .appUser(appUser) // FK to Spring Security user
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        return userCredentialRepository.save(cred);
    }

    // ─────────────────────────────────────────────
    // DTO mapping
    // ─────────────────────────────────────────────
    private UserCredentialResponse toCredentialsResponse(UserCredential credential) {
        return UserCredentialResponse.builder()
                .userId(credential.getId())           // If your DTO meaning is "credentialId", consider renaming field
                .accountId(credential.getAccountId())
                .baseUrl(credential.getBaseUrl())
                .token(credential.getToken())
                .username(credential.getUsername())
                .createdAt(credential.getCreatedAt())
                .build();
    }
}

package com.pw.edu.pl.master.thesis.user.controller;

import com.pw.edu.pl.master.thesis.user.dto.credentials.UserCredentialRequest;
import com.pw.edu.pl.master.thesis.user.dto.credentials.UserCredentialResponse;
import com.pw.edu.pl.master.thesis.user.dto.user.TokenRequest;
import com.pw.edu.pl.master.thesis.user.dto.user.TokenResponse;
import com.pw.edu.pl.master.thesis.user.dto.user.UserSummary;
import com.pw.edu.pl.master.thesis.user.model.user.UserCredential;
import com.pw.edu.pl.master.thesis.user.service.CredentialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/wut/credentials")
@RequiredArgsConstructor
@Validated
public class UserCredentialController {

    private final CredentialService credentialService;

    // ─────────────── CREATE ───────────────
    /** Create & store a credential (service normalizes base URL and encrypts token). */
    @PostMapping
    public ResponseEntity<UserCredentialResponse> addCredential(@RequestBody UserCredentialRequest credential) {
        return ResponseEntity.status(HttpStatus.CREATED).body(credentialService.addCredential(credential));
    }

    // ─────────────── READ (CURRENT USER) ───────────────
    /** Get CURRENT caller’s credential. */
    @GetMapping("/me")
    public ResponseEntity<UserCredentialResponse> getMine() {
        UserCredential cred = credentialService.getForCurrentUserOrThrow();
        UserCredentialResponse dto = UserCredentialResponse.builder()
                .userId(cred.getId())
                .accountId(cred.getAccountId())
                .baseUrl(cred.getBaseUrl())
                .token(cred.getToken())
                .username(cred.getUsername())
                .createdAt(cred.getCreatedAt())
                .build();
        return ResponseEntity.ok(dto);
    }

    // ─────────────── READ (ADMIN/UTILITY) ───────────────
    /** Get credential (DTO) by Jira username (admin/utility). */
    @GetMapping("/by-username")
    public ResponseEntity<UserCredentialResponse> getByUsername(@RequestParam String username) {
        return ResponseEntity.ok(credentialService.getCredentialByUserName(username));
    }

    /** Get credential (DTO) by credential id (admin/utility). */
    @GetMapping("/{credentialId}")
    public ResponseEntity<UserCredentialResponse> getById(@PathVariable Long credentialId) {
        return ResponseEntity.ok(credentialService.findByCredentialId(credentialId));
    }

    /** Resolve application UserSummary by Jira accountId (admin/utility). */
    @GetMapping("/user-summary/by-account-id")
    public ResponseEntity<UserSummary> getUserSummaryByAccountId(@RequestParam String accountId) {
        return ResponseEntity.ok(credentialService.findByAccountId(accountId));
    }

    // ─────────────── UPDATE (CURRENT USER) ───────────────
    /** Update the base URL tied to the CURRENT user's credential. */
    @PutMapping("/base-url")
    public ResponseEntity<UserCredentialResponse> updateBaseURLForCurrentUser(
            @RequestParam(name = "oldURL") String oldURL,
            @RequestParam(name = "newURL") String newURL
    ) {
        // username param removed — service reads Basic principal
        return ResponseEntity.ok(credentialService.updateBaseUrlForCurrentUser(null, oldURL, newURL));
    }

    /** Update the API token (CURRENT user). Request contains plain token; service encrypts. */
    @PutMapping("/token")
    public ResponseEntity<UserCredentialResponse> updateToken(@RequestBody TokenRequest request) {
        // username param removed — service reads Basic principal
        return ResponseEntity.ok(credentialService.updateCredentialToken(null, request.getPlainToken()));
    }

    // ─────────────── DELETE ───────────────
    /** Delete CURRENT caller’s credential. */
    @DeleteMapping("/me")
    public ResponseEntity<UserCredentialResponse> deleteMine() {
        String username = credentialService.getForCurrentUserOrThrow().getUsername();
        return ResponseEntity.ok(credentialService.deleteCredential(username));
    }

    /** Admin/utility: delete by explicit username. */
    @DeleteMapping("/by-username")
    public ResponseEntity<UserCredentialResponse> deleteByUsername(@RequestParam String username) {
        return ResponseEntity.ok(credentialService.deleteCredential(username));
    }

    // ─────────────── EXISTENCE (ADMIN/UTILITY) ───────────────
    @GetMapping("/exists/username")
    public ResponseEntity<Boolean> existsByJiraUsername(@RequestParam String username) {
        return ResponseEntity.ok(credentialService.existsByJiraUsername(username));
    }

    @GetMapping("/exists/account-id")
    public ResponseEntity<Boolean> existsByAccountId(@RequestParam String accountId) {
        return ResponseEntity.ok(credentialService.existsByAccountId(accountId));
    }

    // ─────────────── TOKEN HELPERS ───────────────
    @PostMapping("/encrypt/token")
    public ResponseEntity<TokenResponse> encryptToken(@RequestBody TokenRequest request) {
        return ResponseEntity.ok(credentialService.encryptToken(request));
    }

    @PostMapping("/decrypt/token")
    public ResponseEntity<TokenResponse> decryptToken(@RequestBody TokenRequest request) {
        return ResponseEntity.ok(credentialService.decryptToken(request));
    }
}

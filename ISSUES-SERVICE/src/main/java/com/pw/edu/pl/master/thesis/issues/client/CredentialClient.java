package com.pw.edu.pl.master.thesis.issues.client;



import com.pw.edu.pl.master.thesis.issues.configuration.FeignSecurityConfiguration;
import com.pw.edu.pl.master.thesis.issues.dto.credentials.UserCredentialRequest;
import com.pw.edu.pl.master.thesis.issues.dto.credentials.UserCredentialResponse;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.UserSummary;
import com.pw.edu.pl.master.thesis.issues.dto.user.TokenRequest;
import com.pw.edu.pl.master.thesis.issues.dto.user.TokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "USER-SERVICE",
        contextId = "jiraCredentialClient",
        path = "/api/wut/credentials",
        configuration = FeignSecurityConfiguration.class
)
public interface CredentialClient {

    // CREATE
    @PostMapping
    UserCredentialResponse addCredential(@RequestBody UserCredentialRequest credential);

    // READ (current)
    @GetMapping("/me")
    UserCredentialResponse getMine();

    // READ (utility)
    @GetMapping("/by-username")
    UserCredentialResponse getByUsername(@RequestParam("username") String username);

    @GetMapping("/{credentialId}")
    UserCredentialResponse getById(@PathVariable("credentialId") Long credentialId);

    @GetMapping("/user-summary/by-account-id")
    UserSummary getUserSummaryByAccountId(@RequestParam("accountId") String accountId);

    // UPDATE (current)
    @PutMapping("/base-url")
    UserCredentialResponse updateBaseURLForCurrentUser(@RequestParam("oldURL") String oldURL,
                                                       @RequestParam("newURL") String newURL);

    @PutMapping("/token")
    UserCredentialResponse updateToken(@RequestBody TokenRequest request);

    // DELETE
    @DeleteMapping("/me")
    UserCredentialResponse deleteMine();

    @DeleteMapping("/by-username")
    UserCredentialResponse deleteByUsername(@RequestParam("username") String username);

    // EXISTENCE
    @GetMapping("/exists/username")
    Boolean existsByJiraUsername(@RequestParam("username") String username);

    @GetMapping("/exists/account-id")
    Boolean existsByAccountId(@RequestParam("accountId") String accountId);

    // TOKEN HELPERS
    @PostMapping("/encrypt/token")
    TokenResponse encryptToken(@RequestBody TokenRequest request);

    @PostMapping("/decrypt/token")
    TokenResponse decryptToken(@RequestBody TokenRequest request);
}
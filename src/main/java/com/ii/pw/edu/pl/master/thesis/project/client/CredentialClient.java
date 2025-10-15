package com.ii.pw.edu.pl.master.thesis.project.client;


import com.ii.pw.edu.pl.master.thesis.project.dto.credentials.UserCredentialRequest;
import com.ii.pw.edu.pl.master.thesis.project.dto.credentials.UserCredentialResponse;
import com.ii.pw.edu.pl.master.thesis.project.dto.user.JiraUserMeResponse;
import com.ii.pw.edu.pl.master.thesis.project.dto.user.TokenResponse;
import com.ii.pw.edu.pl.master.thesis.project.dto.user.UserSummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient( name = "user-service", contextId = "jiraCredentialClient",
        url = "${user.service.base-url}",  path = "/api/wut/credentials"
)
public interface CredentialClient {

    @PostMapping
    UserCredentialResponse addCredential(@RequestBody UserCredentialRequest request);

    @GetMapping("/me")
    JiraUserMeResponse me(@RequestParam String username);

    @GetMapping("/{id}")
    UserCredentialResponse getById(@PathVariable Long id);

    @GetMapping("/by-user/{userId}")
    UserCredentialResponse getByUserId(@PathVariable Long userId);

    @GetMapping("/by-username")
    UserCredentialResponse getByUsername(@RequestParam String username);

    @GetMapping("/me/username/{username}")
    UserCredentialResponse getCredentialByUserName(@PathVariable String username);

    @GetMapping("/user-summary/by-account-id")
     UserSummary getUserSummaryByAccountId(@RequestParam String accountId);

    @PutMapping("/me/base-url")
    UserCredentialResponse updateBaseUrlForCurrentUser(@RequestParam String username,
                                                       @RequestParam String oldUrl,
                                                       @RequestParam String newUrl);
    @DeleteMapping("/by-username")
    UserCredentialResponse deleteByUsername(@RequestParam String username);

    @PostMapping("/encrypt/token")
    TokenResponse encryptToken(@RequestBody String plainText);

    @PostMapping("/decrypt/token")
    TokenResponse decryptToken(@RequestBody String encryptedToken);

}

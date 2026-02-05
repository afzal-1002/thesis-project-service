package com.pw.edu.pl.master.thesis.user.service;

import com.pw.edu.pl.master.thesis.user.dto.user.TokenRequest;
import com.pw.edu.pl.master.thesis.user.dto.user.TokenResponse;
import com.pw.edu.pl.master.thesis.user.dto.credentials.UserCredentialRequest;
import com.pw.edu.pl.master.thesis.user.dto.credentials.UserCredentialResponse;
import com.pw.edu.pl.master.thesis.user.model.user.AppUser;
import com.pw.edu.pl.master.thesis.user.model.user.User;
import com.pw.edu.pl.master.thesis.user.model.user.UserCredential;
import com.pw.edu.pl.master.thesis.user.dto.user.UserSummary;
import org.springframework.stereotype.Service;

@Service
public interface CredentialService {

    UserCredentialResponse addCredential(UserCredentialRequest credential);
    UserCredential getUserCredential(String username);
    UserCredentialResponse getCredentialByUserName(String username);
    UserCredentialResponse findByCredentialId(Long credentialId);
    UserCredentialResponse updateBaseUrlForCurrentUser(String username, String oldUrl, String newUrl);
    UserCredentialResponse deleteCredential(String username);
    boolean existsByJiraUsername(String jiraUsername);
    boolean existsByAccountId(String accountId);
    String findAccountIdByUsername(String username);
    UserCredentialResponse updateCredentialToken(String username, String newPlainToken);
    UserSummary findByAccountId(String accountId);

    TokenResponse decryptToken(TokenRequest encryptedToken);
    TokenResponse encryptToken(TokenRequest plainToken);

    UserCredential getForCurrentUserOrThrow();
    UserCredential getByUsernameOrThrow(String username);

    UserCredential addCredentialAndLinkToUsers(UserCredentialRequest req, User user, AppUser appUser);
}
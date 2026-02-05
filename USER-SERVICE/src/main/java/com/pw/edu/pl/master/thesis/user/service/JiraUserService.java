package com.pw.edu.pl.master.thesis.user.service;



import com.pw.edu.pl.master.thesis.user.dto.user.JiraUserMeResponse;
import com.pw.edu.pl.master.thesis.user.model.user.UserCredential;
import com.pw.edu.pl.master.thesis.user.dto.user.JiraUserResponse;
import org.springframework.stereotype.Service;

@Service
public interface JiraUserService {

    JiraUserResponse getMyJiraProfile();

    JiraUserResponse getJiraUserByUsername(String jiraUsername);
    UserCredential getJiraCredential(String jiraUsername);
    JiraUserMeResponse findJiraUserByUserName(String username);

    JiraUserResponse getJiraUserDetails(String baseUrl, String jiraUsername, String encryptedOrDbToken);
    JiraUserResponse getJiraUserDetailsRaw(String baseUrl, String jiraUsername, String plainToken);
}

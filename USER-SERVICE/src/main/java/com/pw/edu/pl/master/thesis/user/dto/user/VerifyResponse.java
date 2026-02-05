package com.pw.edu.pl.master.thesis.user.dto.user;

import java.util.List;

public record VerifyResponse(Long userId, String username, List<String> roles,
                             String jiraAccountId, String jiraBaseUrl) {}


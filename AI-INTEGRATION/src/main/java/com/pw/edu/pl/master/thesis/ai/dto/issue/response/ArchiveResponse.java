package com.pw.edu.pl.master.thesis.ai.dto.issue.response;


import lombok.Data;

import java.util.List;

/**
 * Response from PUT /issue/archive and /issue/unarchive
 */
@Data
public class ArchiveResponse {
    private List<ArchiveIssueResult> issues;

    @Data
    public static class ArchiveIssueResult {
        private String issueIdOrKey;
        private String status;
    }
}


package com.pw.edu.pl.master.thesis.issues.dto.issue.response;


import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.IssueResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response from POST /issue/bulkfetch
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueResponseList {
    private List<IssueResponse> issues;
}


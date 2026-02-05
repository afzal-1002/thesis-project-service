package com.pw.edu.pl.master.thesis.issues.dto.issue.request;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;




import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor @Getter @Setter
public class JqlSearchRequest {

    private String jql;
    private Integer maxResults; // Default: 50
    private String nextPageToken; // For cursor-based pagination


    private String expand; // e.g., "names,changelog"
    private List<String> fields; // e.g., ["summary", "comment"]
    private Boolean fieldsByKeys; // Default: false, reference fields by key instead of ID
    private List<String> properties; // List of issue properties to include

    // Feature for stronger consistency
    private List<Integer> reconcileIssues; // Accepts max 50 issue IDs
}
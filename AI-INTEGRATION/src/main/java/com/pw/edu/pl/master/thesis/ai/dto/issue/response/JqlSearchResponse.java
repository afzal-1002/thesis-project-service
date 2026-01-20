package com.pw.edu.pl.master.thesis.ai.dto.issue.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pw.edu.pl.master.thesis.ai.dto.issue.response.Issuereponse.IssueResponse;
import lombok.*;

import java.util.List;
import java.util.Map;

@Data @Getter
@Setter @Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class JqlSearchResponse {
    private Integer startAt;
    private Integer maxResults;
    private Integer total;
    private List<IssueResponse> issues;
    private Map<String, String> names;
    private List<String> warningMessages;
    private String expand;
}

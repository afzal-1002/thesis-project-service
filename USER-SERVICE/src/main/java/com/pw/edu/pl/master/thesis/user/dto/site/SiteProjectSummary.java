package com.pw.edu.pl.master.thesis.user.dto.site;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class SiteProjectSummary {
    private Long id;
    private String jiraId;
    private String projectKey;
    private String projectName;
}
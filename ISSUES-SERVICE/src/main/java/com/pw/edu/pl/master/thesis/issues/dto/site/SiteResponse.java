package com.pw.edu.pl.master.thesis.issues.dto.site;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor
@AllArgsConstructor @Builder
public class SiteResponse {
    private Long id;
    private String siteName;
    private String hostPart;
    private String baseURL;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    // optional — include related projects summary
    private List<SiteProjectSummary> projects;
}
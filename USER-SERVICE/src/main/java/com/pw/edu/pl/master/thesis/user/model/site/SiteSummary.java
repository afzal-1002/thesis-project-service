package com.pw.edu.pl.master.thesis.user.model.site;


import lombok.*;

import java.time.OffsetDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SiteSummary {
    private Long id;
    private String siteName;
    private String baseUrl;
    private OffsetDateTime addedAt;
}

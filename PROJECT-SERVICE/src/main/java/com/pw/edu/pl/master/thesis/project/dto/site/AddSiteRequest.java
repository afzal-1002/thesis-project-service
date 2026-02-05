package com.pw.edu.pl.master.thesis.project.dto.site;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddSiteRequest {
    private String siteName;
    private String hostPart;
    private String baseUrl;
}
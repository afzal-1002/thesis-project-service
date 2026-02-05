package com.pw.edu.pl.master.thesis.user.dto.site;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder @ToString
public class AddSiteRequest {
    private String siteName;
    private String username;
    private String hostPart;
    private String baseUrl;
    private String jiraToken;
}

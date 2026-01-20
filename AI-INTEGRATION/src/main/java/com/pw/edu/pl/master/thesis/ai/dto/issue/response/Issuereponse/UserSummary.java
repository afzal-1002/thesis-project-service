package com.pw.edu.pl.master.thesis.ai.dto.issue.response.Issuereponse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserSummary {
    private String self;
    private String username;
    private String id;
    private String accountId;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private String displayName;
    private Boolean active;
    private String timeZone;
    private String locale;
    private String accountType;
    private AvatarUrls avatarUrls;
}

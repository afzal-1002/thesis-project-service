package com.pw.edu.pl.master.thesis.user.model.project;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pw.edu.pl.master.thesis.user.dto.AvatarUrls;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectSummary {
    private String expand;
    private String self;

    private String id;
    private String key;             // e.g. "ABC"
    private String name;            // e.g. "Payments"

    private String projectTypeKey;
    private Boolean simplified;
    private Boolean isPrivate;

    private AvatarUrls avatarUrls;  // optional
}

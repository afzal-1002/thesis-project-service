package com.pw.edu.pl.master.thesis.issues.dto.project;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pw.edu.pl.master.thesis.issues.dto.common.AvatarUrls;
// If you really have a DTO for properties, keep it and remove the Map import.
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectSummary {
    private String expand;
    private String self;

    private String id;
    private String key;
    private String name;
    private String projectTypeKey;

    private Boolean simplified;

    private String style;

    @JsonProperty("isPrivate")
    private Boolean isPrivate;

    private Map<String, Object> properties;

    private AvatarUrls avatarUrls;
}

package com.pw.edu.pl.master.thesis.ai.dto.project;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pw.edu.pl.master.thesis.ai.dto.common.AvatarUrls;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

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

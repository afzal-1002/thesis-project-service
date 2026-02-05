package com.pw.edu.pl.master.thesis.project.dto.project;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pw.edu.pl.master.thesis.project.model.common.AvatarUrls;
import com.pw.edu.pl.master.thesis.project.model.common.Properties;
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
    private String simplified;
    private String style;
    private String isPrivate;
    private Properties properties ;

    private AvatarUrls avatarUrls;
}
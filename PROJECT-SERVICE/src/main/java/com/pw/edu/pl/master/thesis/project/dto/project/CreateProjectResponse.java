package com.pw.edu.pl.master.thesis.project.dto.project;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Data
@NoArgsConstructor
@Getter
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateProjectResponse {
    private String self;
    private String id;
    private String key;
    private String name;
    private String projectTypeKey;
    private String projectTemplateKey;
    private String description;
}
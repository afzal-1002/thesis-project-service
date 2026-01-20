package com.pw.edu.pl.master.thesis.ai.dto.project;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor
@AllArgsConstructor
public class ProjectTemplate {
    private String projectTemplateKey;
    private String name;
    private String description;
}

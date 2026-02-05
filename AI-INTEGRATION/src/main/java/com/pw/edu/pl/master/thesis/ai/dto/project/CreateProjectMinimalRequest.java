package com.pw.edu.pl.master.thesis.ai.dto.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProjectMinimalRequest {
    private String key;                 // required
    private String name;                // required
    private String projectTypeKey;      // required: software | business | service_desk
    private String description;         // optional

    private String templateKey;         // optional: if FE picked a specific template from dropdown
    private String templateNamePreferred; // optional: "Scrum", "Kanban", "Project", "Service"
}
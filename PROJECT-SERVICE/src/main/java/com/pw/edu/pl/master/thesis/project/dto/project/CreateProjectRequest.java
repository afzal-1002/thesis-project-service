package com.pw.edu.pl.master.thesis.project.dto.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor
@AllArgsConstructor
public class CreateProjectRequest {
    private String key;
    private String projectName;
    private String projectTypeKey;
    private String description;
    private String assigneeType;       // optional: "PROJECT_LEAD" | "UNASSIGNED" (defaulted to PROJECT_LEAD)
    private String leadAccountId;      // optional
    private String templateKey;        // optional: picked by FE; if blank we auto-pick
    private String templateNamePreferred; // optional: "Scrum", "Kanban", etc. (used only when templateKey is blank)

}
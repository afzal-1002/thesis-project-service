package com.pw.edu.pl.master.thesis.ai.dto.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor
@AllArgsConstructor
public class CreateProjectRequest {
    private String key;
    private String projectName;               // e.g. "Bug Demo Project"
    private String projectTypeKey;     // "software" | "business" | "service_desk"
    private String description;        // optional
    private String assigneeType;       // optional: "PROJECT_LEAD" | "UNASSIGNED" (defaulted to PROJECT_LEAD)
    private String username;
    private String leadAccountId;      // optional
    private String templateKey;        // optional: picked by FE; if blank we auto-pick
    private String templateNamePreferred; // optional: "Scrum", "Kanban", etc. (used only when templateKey is blank)

}
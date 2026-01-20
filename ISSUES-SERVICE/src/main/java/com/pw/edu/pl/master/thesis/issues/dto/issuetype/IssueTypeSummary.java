package com.pw.edu.pl.master.thesis.issues.dto.issuetype;

import lombok.*;

@Data @Generated
@NoArgsConstructor @AllArgsConstructor @Builder
public class IssueTypeSummary {
    private String self;
    private Long id;
    private String name;
    private String untranslatedName;
    private String description;
    private String iconUrl;
    private boolean subtask;
    private Integer avatarId;
    private int hierarchyLevel;

    private Scope scope;

    public Boolean getSubtask() {
        return subtask;
    }

    @Data
    @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Scope {
        private String type;
        private Project project;

        @Data
        @NoArgsConstructor @AllArgsConstructor @Builder
        public static class Project {
            private String id;
        }
    }
}

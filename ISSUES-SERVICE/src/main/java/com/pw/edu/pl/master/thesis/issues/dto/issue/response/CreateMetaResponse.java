package com.pw.edu.pl.master.thesis.issues.dto.issue.response;


import lombok.Data;

import java.util.List;

/**
 * Response from GET /issue/createmeta (global) and
 * GET /issue/createmeta/{project}/issuetypes
 */
@Data
public class CreateMetaResponse {
    private List<ProjectMeta> projects;

    @Data
    public static class ProjectMeta {
        private String key;
        private String name;
        /** Raw JSON array “issuetypes” */
        private List<IssueTypeMeta> issuetypes;
    }

    @Data
    public static class IssueTypeMeta {
        private String id;
        private String name;
        private String description;
        private boolean subtask;
        private String avatarId;
        private Integer hierarchyLevel;
        private String self;
    }
}

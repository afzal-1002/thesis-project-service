package com.pw.edu.pl.master.thesis.ai.dto.issuetype;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data @Getter
@Setter
@ToString
public class CreateIssueTypeResponse {

    private String    self;
    @Id
    private String    id;
    @Column(columnDefinition = "TEXT")
    private String    description;
    private String    iconUrl;
    @Column(nullable = false)
    private String    name;
    private String    untranslatedName;
    private boolean   subtask;
    private Integer      avatarId;
    private Integer   hierarchyLevel;
    private Scope scope;

    public Boolean getSubtask() {
        return subtask;
    }

    @Data
    public static class Scope {
        private String        type;
        private Project       project;
    }

    @Data
    public static class Project {
        private String        id;
        private String        key;
        private String        name;
    }
}
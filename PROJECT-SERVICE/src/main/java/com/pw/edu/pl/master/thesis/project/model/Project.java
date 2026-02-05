package com.pw.edu.pl.master.thesis.project.model;


import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Long id;

    @Column(name = "jira_id")
    private String jiraId;

    @Column(name = "project_key", nullable = false, length = 32)
    private String key;

    @Column(name = "base_url")
    private String baseUrl;

    @Column(name = "project_name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "project_category")
    private String projectCategory;

    @Column(name = "project_type_key")
    private String projectTypeKey;

    // External reference to user-service
    @Column(name = "lead_user_id", length = 64)
    private String leadUserId;

    // Link entities (local FK only; no external FKs)
    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ProjectUser> projectUsers = new HashSet<>();

    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ProjectIssue> projectIssues = new HashSet<>();

    /* --------- Optional tiny helpers (safe to keep) --------- */

    public void addProjectUser(ProjectUser link) {
        if (link == null) return;
        link.setProject(this);
        projectUsers.add(link);
    }

    public void removeProjectUser(ProjectUser link) {
        if (link == null) return;
        projectUsers.remove(link);
        link.setProject(null);
    }

    public void addProjectIssue(ProjectIssue link) {
        if (link == null) return;
        link.setProject(this);
        projectIssues.add(link);
    }

    public void removeProjectIssue(ProjectIssue link) {
        if (link == null) return;
        projectIssues.remove(link);
        link.setProject(null);
    }

}

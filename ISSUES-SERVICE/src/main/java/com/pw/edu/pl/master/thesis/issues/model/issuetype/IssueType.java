package com.pw.edu.pl.master.thesis.issues.model.issuetype;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "issue_types",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_issue_types_jira_id", columnNames = "jira_id"),
                @UniqueConstraint(name = "uk_issue_types_name_ci", columnNames = "name")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class IssueType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                 // DB PK (auto)

    @Column(name = "jira_id", length = 64, unique = true)
    private String jiraId;           // Jira’s issue type id (string)

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "icon_url", length = 512)
    private String iconUrl;

    @Column(name = "subtask")
    private Boolean subtask;

    @Column(name = "avatar_id")
    private Integer avatarId;

    @Column(name = "hierarchy_level")
    private Integer hierarchyLevel;

    @Column(name = "self")
    private String self;


}

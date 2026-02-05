package com.pw.edu.pl.master.thesis.project.model;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "project_issue")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProjectIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ElementCollection
    @CollectionTable(name = "project_issue_keys", joinColumns = @JoinColumn(name = "project_issue_id"))
    @Column(name = "issue_key", length = 64, nullable = false)
    private java.util.List<String> issueKey;
}


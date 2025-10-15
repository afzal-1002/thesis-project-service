package com.ii.pw.edu.pl.master.thesis.project.model;


import jakarta.persistence.*;
import lombok.*;

import java.util.List;

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

    @Column(name = "issue_key", nullable = false, length = 64)
    private List<String> issueKey;
}

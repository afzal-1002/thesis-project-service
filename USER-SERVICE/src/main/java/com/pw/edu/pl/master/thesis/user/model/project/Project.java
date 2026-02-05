package com.pw.edu.pl.master.thesis.user.model.project;

import jakarta.persistence.Column;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    @Column(name = "jira_id")
    private String jiraId;

    @Column(name = "project_key", unique = true, nullable = false, length = 32)
    private String key;

    @Column(name = "project_name", nullable = false, length = 200)
    private String name;

    @Column(name = "base_url")
    private String baseUrl;
}

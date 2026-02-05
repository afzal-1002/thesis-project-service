package com.pw.edu.pl.master.thesis.user.model.project;

import com.pw.edu.pl.master.thesis.user.model.user.User;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table( name = "user_projects",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "project_key"})
)
public class UserProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "project_key", nullable = false, length = 64)
    private String projectKey;

    @Column(name = "role_in_project")
    private String roleInProject;

    protected UserProject() {}
    public UserProject(User user, String projectKey, String roleInProject) {
        this.user = user;
        this.projectKey = projectKey;
        this.roleInProject = roleInProject;
    }

}
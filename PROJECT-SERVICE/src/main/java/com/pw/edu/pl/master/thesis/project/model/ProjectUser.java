package com.pw.edu.pl.master.thesis.project.model;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "project_user")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProjectUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // local FK only
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // external id from user-service
    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(name = "user_name", nullable = false, length = 64)
    private String username;
}

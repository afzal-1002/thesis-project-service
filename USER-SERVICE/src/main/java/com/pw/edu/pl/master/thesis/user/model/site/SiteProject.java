package com.pw.edu.pl.master.thesis.user.model.site;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "site_project")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class SiteProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "site_project_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "site_id", nullable = false, foreignKey = @ForeignKey(name = "fk_site_project_site"))
    private Site site;

    @Column(name = "jira_id", length = 80)
    private String jiraId;               // external system id

    @Column(name = "project_key", nullable = false, length = 32)
    private String key;                  // e.g., "ABC"

    @Column(name = "project_name", nullable = false, length = 200)
    private String name;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}

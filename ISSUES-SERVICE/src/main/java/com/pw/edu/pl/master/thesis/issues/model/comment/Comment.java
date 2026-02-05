package com.pw.edu.pl.master.thesis.issues.model.comment;

import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.Visibility;
import com.pw.edu.pl.master.thesis.issues.enums.PushStatus;
import com.pw.edu.pl.master.thesis.issues.enums.SynchronizationStatus;
import com.pw.edu.pl.master.thesis.issues.model.issue.Issue;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "comments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder(toBuilder = true)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "comment_id", unique = true)
    private String commentId;

    @Column(name = "jira_self_url", columnDefinition = "TEXT")
    private String self;

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status")
    private SynchronizationStatus synchronizationStatus;

    // --- Relations ---
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;

    @Column(name = "author_id")
    private String authorId;

    @Column(name = "updated_by_user_id")
    private String updatedByUserId;

    @Column(name = "body_json", columnDefinition = "TEXT", nullable = false)
    private String body;


    @Transient
    private Visibility visibility;


    @Column(name = "jsd_public")
    private Boolean jsdPublic;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    // ===================== AI-related additions =====================

    @Column(name = "ai_generated", nullable = false)
    private boolean aiGenerated;

    @Enumerated(EnumType.STRING)
    @Column(name = "push_status", length = 16)
    private PushStatus pushStatus;

    @Column(name = "ai_job_id", length = 64)
    private String aiJobId;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (updatedAt == null) updatedAt = createdAt;
    }
    @PreUpdate
    void onUpdate() { updatedAt = OffsetDateTime.now(); }

}

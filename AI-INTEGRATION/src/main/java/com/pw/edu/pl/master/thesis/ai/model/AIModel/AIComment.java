package com.pw.edu.pl.master.thesis.ai.model.AIModel;

import com.pw.edu.pl.master.thesis.ai.enums.SynchronizationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "ai_comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "issue_key")
    private String issueKey;

    @Column(name = "source_comment_id")
    private String sourceCommentId;

    @Column(name = "title")
    private String title;

    @Column(name = "prompt", columnDefinition = "TEXT")
    private String prompt;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "analysis", columnDefinition = "TEXT")
    private String analysis;

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status")
    private SynchronizationStatus syncStatus;

    @Column(name = "pushed_comment_id")
    private String pushedCommentId;

    @Builder.Default
    private Boolean selected = false;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = OffsetDateTime.now();
        updatedAt = createdAt;
        if (syncStatus == null) syncStatus = SynchronizationStatus.PENDING;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}

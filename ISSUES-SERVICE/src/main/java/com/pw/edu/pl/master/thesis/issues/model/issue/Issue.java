package com.pw.edu.pl.master.thesis.issues.model.issue;

import com.pw.edu.pl.master.thesis.issues.enums.SynchronizationStatus;
import com.pw.edu.pl.master.thesis.issues.model.comment.Comment;
import com.pw.edu.pl.master.thesis.issues.model.issuetype.IssueType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "issues",
        indexes = {
                @Index(name = "idx_issues_project_key", columnList = "project_key"),
                @Index(name = "idx_issues_sync_status", columnList = "sync_status")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_issues_issue_key", columnNames = "issue_key")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Issue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "jira_id", length = 64)
    private String jiraId;

    @Column(name = "self")
    private String self;

    @Column(name = "issue_key", nullable = false, length = 64, unique = true)
    private String key;

    @Column(name = "summary", length = 512)
    private String summary;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "project_key", nullable = false, length = 64)
    private String projectKey;

    @Column(name = "reporter_id", length = 128)
    private String reporterId;

    @Column(name = "assignee_id", length = 128)
    private String assigneeId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "due_date")
    private LocalDate dueDate;

    // in Issue entity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_type_id", nullable = true)
    private IssueType issueType;

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status", nullable = false, length = 32)
    private SynchronizationStatus syncStatus;

    @OneToOne(mappedBy = "issue", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private IssueTimeTracking timeTracking;

    // NEW: comments live under Issue; AI estimation now hangs off Comment (not Issue)
    @OneToMany(mappedBy = "issue", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Comment> comments = new ArrayList<>();

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (syncStatus == null) syncStatus = SynchronizationStatus.PENDING;
    }

    @PreUpdate
    void onUpdate() { updatedAt = LocalDateTime.now();}

    public void setTimeTracking(IssueTimeTracking tt) {
        if (this.timeTracking != null) {
            this.timeTracking.setIssue(null);
        }
        this.timeTracking = tt;
        if (tt != null) {
            tt.setIssue(this); // <-- IMPORTANT: set owning side
        }
    }

}

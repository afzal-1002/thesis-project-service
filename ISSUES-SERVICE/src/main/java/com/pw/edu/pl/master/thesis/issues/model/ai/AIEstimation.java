package com.pw.edu.pl.master.thesis.issues.model.ai;

import com.pw.edu.pl.master.thesis.issues.model.issue.Issue;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_estimations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIEstimation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "model_name")
    private String modelName;

    @Column(name = "prompt", columnDefinition = "TEXT")
    private String prompt;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "estimation_minutes")
    private Integer estimationMinutes;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;

    @Column(name = "created_by")
    private String createdBy;
}
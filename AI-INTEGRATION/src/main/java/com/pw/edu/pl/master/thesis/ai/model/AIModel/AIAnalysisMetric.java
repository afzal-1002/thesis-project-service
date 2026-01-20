package com.pw.edu.pl.master.thesis.ai.model.AIModel;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "ai_analysis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIAnalysisMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String issueKey;

    @Column(columnDefinition = "LONGTEXT")
    private String userPrompt;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @Column(nullable = false)
    private String aiProvider;

    @Column(nullable = false)
    private String aiModel;

    @Column(nullable = false)
    private Long analysisTimeMs;

    @Column(nullable = false)
    private Double analysisTimeSec;

    private Integer estimatedResolutionHours;
    private Double estimatedResolutionDays;

    // ✅ GROUND TRUTH (CRITICAL FOR RESEARCH)
    private Double actualResolutionHours;

    private Boolean markdownEnabled;
    private Boolean explanationEnabled;

    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = OffsetDateTime.now();
    }
}

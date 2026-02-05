package com.pw.edu.pl.master.thesis.issues.model.issue;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "issue_time_tracking")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class IssueTimeTracking {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id", nullable = false) // owning side
    private Issue issue;


    @Column(name = "time_spent")
    private Integer timeSpent;

    private String originalEstimate;
    private String remainingEstimate;
    private Integer originalEstimateSeconds;
    private Integer remainingEstimateSeconds;

    @Column(name = "ai_estimate_minutes")
    private Integer aiEstimateMinutes;

}

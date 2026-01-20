package com.pw.edu.pl.master.thesis.ai.model;

import lombok.Data;

@Data
public class AIModelComparisonResult {

    private String aiProvider;

    // Performance
    private Double avgResponseTimeSec;
    private Double minResponseTimeSec;
    private Double maxResponseTimeSec;
    private Double stdDeviationResponseTime;

    // Estimation
    private Double avgEstimatedHours;
    private Double minEstimatedHours;
    private Double maxEstimatedHours;

    // Content Quality
    private Double avgResponseLength;
    private Double engineeringRelevanceScore;

    // Stability
    private Double stabilityScore;
}

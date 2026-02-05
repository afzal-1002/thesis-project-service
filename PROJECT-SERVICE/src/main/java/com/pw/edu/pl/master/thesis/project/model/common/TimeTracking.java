package com.pw.edu.pl.master.thesis.project.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TimeTracking {
    private String originalEstimate;
    private int    originalEstimateSeconds;
    private String remainingEstimate;
    private int    remainingEstimateSeconds;
    private String timeSpent;
    private int    timeSpentSeconds;
    private int aiEstimationSeconds;
}

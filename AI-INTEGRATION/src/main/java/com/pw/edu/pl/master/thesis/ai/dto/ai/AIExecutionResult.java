package com.pw.edu.pl.master.thesis.ai.dto.ai;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIExecutionResult {

    private String aiProvider;
    private String aiModel;

    private long analysisTimeMs;
    private double analysisTimeSec;

    private Integer estimatedResolutionHours;
    private Double estimatedResolutionDays;

    private String content;
}

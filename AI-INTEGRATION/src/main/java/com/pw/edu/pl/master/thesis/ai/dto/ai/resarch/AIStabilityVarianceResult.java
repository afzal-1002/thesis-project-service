package com.pw.edu.pl.master.thesis.ai.dto.ai.resarch;


import lombok.Data;

@Data
public class AIStabilityVarianceResult {

    private String aiProvider;

    private Double estimationVariance;
    private Double responseTimeVariance;
}

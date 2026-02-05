package com.pw.edu.pl.master.thesis.ai.dto.ai.resarch;


import lombok.Data;

@Data
public class AIExplainabilityTradeoffResult {

    private String aiProvider;

    private Boolean explanationEnabled;

    private Double avgResponseTimeSec;
    private Double avgEngineeringScore;
}

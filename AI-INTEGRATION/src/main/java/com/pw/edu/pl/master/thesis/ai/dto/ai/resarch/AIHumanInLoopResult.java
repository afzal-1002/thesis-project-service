package com.pw.edu.pl.master.thesis.ai.dto.ai.resarch;


import lombok.Data;

@Data
public class AIHumanInLoopResult {

    private String aiProvider;

    private Boolean userPromptProvided;

    private Double avgEngineeringScore;
    private Double avgEstimatedHours;
}

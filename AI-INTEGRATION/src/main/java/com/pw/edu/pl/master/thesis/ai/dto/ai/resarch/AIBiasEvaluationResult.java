package com.pw.edu.pl.master.thesis.ai.dto.ai.resarch;

import lombok.Data;

@Data
public class AIBiasEvaluationResult {

    private String aiProvider;

    private Double meanAbsoluteError;
    private Double meanSquaredError;
    private Double biasScore; // +ve = overestimation, -ve = underestimation
}

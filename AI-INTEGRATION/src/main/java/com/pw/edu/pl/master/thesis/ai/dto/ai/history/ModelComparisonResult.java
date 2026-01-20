package com.pw.edu.pl.master.thesis.ai.dto.ai.history;

import lombok.Data;

@Data
public class ModelComparisonResult {
    private String issueKey;
    private Integer geminiEstimate;
    private Integer deepSeekEstimate;
    private Double actualHours;
    private String betterModel;
}

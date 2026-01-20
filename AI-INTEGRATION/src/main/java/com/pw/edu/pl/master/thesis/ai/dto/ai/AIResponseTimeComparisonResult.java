package com.pw.edu.pl.master.thesis.ai.dto.ai;


import lombok.Data;

@Data
public class AIResponseTimeComparisonResult {

    private String aiProvider;

    private Double avgResponseTimeSec;
    private Double minResponseTimeSec;
    private Double maxResponseTimeSec;

    private Double stdDeviationSec;

    private Integer sampleCount;
}

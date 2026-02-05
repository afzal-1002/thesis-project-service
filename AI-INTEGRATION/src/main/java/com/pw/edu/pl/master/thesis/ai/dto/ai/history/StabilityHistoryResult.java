package com.pw.edu.pl.master.thesis.ai.dto.ai.history;

public record StabilityHistoryResult(
        String aiProvider,
        Double estimationVariance,
        Double responseTimeVariance
) {}

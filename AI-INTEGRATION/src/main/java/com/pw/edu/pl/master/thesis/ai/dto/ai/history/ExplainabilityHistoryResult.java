package com.pw.edu.pl.master.thesis.ai.dto.ai.history;

public record ExplainabilityHistoryResult(
        String aiProvider,
        Boolean explanationEnabled,
        Double avgError,
        Double avgResponseTime
) {}

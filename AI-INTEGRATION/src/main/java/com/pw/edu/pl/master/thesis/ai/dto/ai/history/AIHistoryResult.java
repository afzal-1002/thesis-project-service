package com.pw.edu.pl.master.thesis.ai.dto.ai.history;

public record AIHistoryResult(
        String issueKey,
        String aiProvider,
        Integer estimatedHours,
        Double actualHours,
        Double errorHours,
        Double analysisTimeSec,
        Boolean explanationEnabled,
        Boolean userPromptProvided,
        Object createdAt
) {}

package com.pw.edu.pl.master.thesis.ai.dto.ai.history;

public record HumanInLoopHistoryResult(
        String aiProvider,
        Boolean userPromptProvided,
        Double avgEstimatedHours
) {}

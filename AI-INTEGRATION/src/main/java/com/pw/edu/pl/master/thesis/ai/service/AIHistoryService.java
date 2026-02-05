package com.pw.edu.pl.master.thesis.ai.service;

import com.pw.edu.pl.master.thesis.ai.dto.ai.history.*;

import java.util.List;

public interface AIHistoryService {

    List<AIHistoryResult> estimationHistory(
            String issueKey,
            String aiProvider,
            Boolean explanationEnabled,
            Boolean userPromptProvided
    );

    List<AccuracyTrendResult> accuracyTrend(String aiProvider);

    List<ModelComparisonResult> modelComparison();

    List<ExplainabilityHistoryResult> explainabilityImpact();

    List<HumanInLoopHistoryResult> humanInLoopHistory();

    List<StabilityHistoryResult> stabilityHistory();

    AIResponseArchive rawResponse(String issueKey);
}

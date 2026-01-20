package com.pw.edu.pl.master.thesis.ai.service;



import com.pw.edu.pl.master.thesis.ai.dto.ai.resarch.AIBiasEvaluationResult;
import com.pw.edu.pl.master.thesis.ai.dto.ai.resarch.AIExplainabilityTradeoffResult;
import com.pw.edu.pl.master.thesis.ai.dto.ai.resarch.AIHumanInLoopResult;
import com.pw.edu.pl.master.thesis.ai.dto.ai.resarch.AIStabilityVarianceResult;

import java.util.List;
import java.util.Map;

public interface AIResearchEvaluationService {

    List<AIBiasEvaluationResult> biasAnalysis();

    List<AIExplainabilityTradeoffResult> explainabilityTradeoff();

    List<AIStabilityVarianceResult> stabilityVariance();

    List<AIHumanInLoopResult> humanInLoopImpact();

    Map<String, Object> hybridStrategyRecommendation();

    Map<String, Object> researchSummary();
}

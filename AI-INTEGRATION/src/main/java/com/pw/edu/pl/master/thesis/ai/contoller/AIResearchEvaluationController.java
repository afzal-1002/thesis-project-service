package com.pw.edu.pl.master.thesis.ai.contoller;

import com.pw.edu.pl.master.thesis.ai.dto.ai.resarch.AIBiasEvaluationResult;
import com.pw.edu.pl.master.thesis.ai.dto.ai.resarch.AIExplainabilityTradeoffResult;
import com.pw.edu.pl.master.thesis.ai.dto.ai.resarch.AIHumanInLoopResult;
import com.pw.edu.pl.master.thesis.ai.dto.ai.resarch.AIStabilityVarianceResult;
import com.pw.edu.pl.master.thesis.ai.service.AIResearchEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wut/ai/research")
@RequiredArgsConstructor
public class AIResearchEvaluationController {

    private final AIResearchEvaluationService service;

    @GetMapping("/bias")
    public List<AIBiasEvaluationResult> bias() {
        return service.biasAnalysis();
    }

    @GetMapping("/explainability-tradeoff")
    public List<AIExplainabilityTradeoffResult> explainability() {
        return service.explainabilityTradeoff();
    }

    @GetMapping("/stability-variance")
    public List<AIStabilityVarianceResult> stability() {
        return service.stabilityVariance();
    }

    @GetMapping("/human-in-loop")
    public List<AIHumanInLoopResult> humanInLoop() {
        return service.humanInLoopImpact();
    }

    @GetMapping("/hybrid-strategy")
    public Map<String, Object> hybrid() {
        return service.hybridStrategyRecommendation();
    }

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        return service.researchSummary();
    }
}

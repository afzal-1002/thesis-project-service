package com.pw.edu.pl.master.thesis.ai.contoller;


import com.pw.edu.pl.master.thesis.ai.dto.ai.history.*;
import com.pw.edu.pl.master.thesis.ai.service.AIHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wut/ai/history")
@RequiredArgsConstructor
public class AIHistoryController {

    private final AIHistoryService service;

    @GetMapping("/estimations")
    public List<AIHistoryResult> estimations(
            @RequestParam(required = false) String issueKey,
            @RequestParam(required = false) String aiProvider,
            @RequestParam(required = false) Boolean explanationEnabled,
            @RequestParam(required = false) Boolean userPromptProvided
    ) {
        return service.estimationHistory(issueKey, aiProvider, explanationEnabled, userPromptProvided);
    }

    @GetMapping("/accuracy-trend")
    public List<AccuracyTrendResult> accuracyTrend(
            @RequestParam(required = false) String aiProvider
    ) {
        return service.accuracyTrend(aiProvider);
    }

    @GetMapping("/model-comparison")
    public List<ModelComparisonResult> modelComparison() {
        return service.modelComparison();
    }

    @GetMapping("/explainability-impact")
    public List<ExplainabilityHistoryResult> explainabilityImpact() {
        return service.explainabilityImpact();
    }

    @GetMapping("/human-in-loop")
    public List<HumanInLoopHistoryResult> humanInLoop() {
        return service.humanInLoopHistory();
    }

    @GetMapping("/stability")
    public List<StabilityHistoryResult> stability() {
        return service.stabilityHistory();
    }

    @GetMapping("/raw/{issueKey}")
    public AIResponseArchive raw(@PathVariable String issueKey) {
        return service.rawResponse(issueKey);
    }
}


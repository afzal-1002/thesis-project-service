package com.pw.edu.pl.master.thesis.ai.contoller;

import com.pw.edu.pl.master.thesis.ai.service.AIAnalysisEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wut/ai/evaluation")
@RequiredArgsConstructor
public class AIAnalysisEvaluationController {

    private final AIAnalysisEvaluationService service;

    @GetMapping("/issue/{issueKey}")
    public Map<String, Object> metricsByIssue(@PathVariable String issueKey) {
        return service.getMetricsForIssue(issueKey);
    }

    @GetMapping("/model/{provider}")
    public Map<String, Object> metricsByProvider(@PathVariable String provider) {
        return service.getMetricsForProvider(provider);
    }

    @GetMapping("/compare")
    public Map<String, Object> compareProviders() {
        return service.compareProviders();
    }

    @GetMapping("/features")
    public Map<String, Object> featureImpact() {
        return service.getFeatureImpactMetrics();
    }

    @GetMapping("/stability/{issueKey}")
    public Map<String, Object> stability(@PathVariable String issueKey) {
        return service.getStabilityMetrics(issueKey);
    }

    // =====================================================
    // 6️⃣ ALL Issues (Dashboard)
    // =====================================================
    @GetMapping("/issues")
    public ResponseEntity<List<Map<String, Object>>> allIssues() {
        return ResponseEntity.ok(service.getAllIssuesMetrics());
    }

    // =====================================================
    // 7️⃣ Overall Summary (Thesis)
    // =====================================================
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> summary() {
        return ResponseEntity.ok(service.getOverallSummary());
    }


}

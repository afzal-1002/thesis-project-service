package com.pw.edu.pl.master.thesis.ai.contoller;

import com.pw.edu.pl.master.thesis.ai.model.AIModel.AIAnalysisMetric;
import com.pw.edu.pl.master.thesis.ai.service.AIAnalysisMetricService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wut/ai/metrics")
@RequiredArgsConstructor
public class AIAnalysisMetricController {

    private final AIAnalysisMetricService service;

    // =====================================================
    // CREATE
    // =====================================================

    @PostMapping
    public ResponseEntity<AIAnalysisMetric> save(
            @RequestBody AIAnalysisMetric metric
    ) {
        return ResponseEntity.ok(service.save(metric));
    }

    // =====================================================
    // DELETE
    // =====================================================

    @DeleteMapping("/issue/{issueKey}")
    public ResponseEntity<Void> deleteByIssueKey(
            @PathVariable String issueKey
    ) {
        service.deleteByIssueKey(issueKey);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/provider/{provider}")
    public ResponseEntity<Void> deleteByProvider(
            @PathVariable String provider
    ) {
        service.deleteByAiProvider(provider);
        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // BASIC FIND
    // =====================================================

    @GetMapping
    public ResponseEntity<List<AIAnalysisMetric>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/issue/{issueKey}")
    public ResponseEntity<List<AIAnalysisMetric>> findByIssueKey(
            @PathVariable String issueKey
    ) {
        return ResponseEntity.ok(service.findByIssueKey(issueKey));
    }

    @GetMapping("/provider/{provider}")
    public ResponseEntity<List<AIAnalysisMetric>> findByProvider(
            @PathVariable String provider
    ) {
        return ResponseEntity.ok(service.findByAiProvider(provider));
    }

    // =====================================================
    // ESTIMATION FILTERS
    // =====================================================

    @GetMapping("/hours/less-than-equal/{hours}")
    public ResponseEntity<List<AIAnalysisMetric>> hoursLessThanEqual(
            @PathVariable Integer hours
    ) {
        return ResponseEntity.ok(service.findHoursLessThanEqual(hours));
    }

    @GetMapping("/hours/greater-than/{hours}")
    public ResponseEntity<List<AIAnalysisMetric>> hoursGreaterThan(
            @PathVariable Integer hours
    ) {
        return ResponseEntity.ok(service.findHoursGreaterThan(hours));
    }

    @GetMapping("/days/less-than-equal/{days}")
    public ResponseEntity<List<AIAnalysisMetric>> daysLessThanEqual(
            @PathVariable Double days
    ) {
        return ResponseEntity.ok(service.findDaysLessThanEqual(days));
    }

    @GetMapping("/days/greater-than/{days}")
    public ResponseEntity<List<AIAnalysisMetric>> daysGreaterThan(
            @PathVariable Double days
    ) {
        return ResponseEntity.ok(service.findDaysGreaterThan(days));
    }

    // =====================================================
    // MARKDOWN FLAGS
    // =====================================================

    @GetMapping("/markdown/true")
    public ResponseEntity<List<AIAnalysisMetric>> markdownTrue() {
        return ResponseEntity.ok(service.markdownTrue());
    }

    @GetMapping("/markdown/false")
    public ResponseEntity<List<AIAnalysisMetric>> markdownFalse() {
        return ResponseEntity.ok(service.markdownFalse());
    }

    // =====================================================
    // EXPLANATION FLAGS
    // =====================================================

    @GetMapping("/explanation/true")
    public ResponseEntity<List<AIAnalysisMetric>> explanationTrue() {
        return ResponseEntity.ok(service.explanationTrue());
    }

    @GetMapping("/explanation/false")
    public ResponseEntity<List<AIAnalysisMetric>> explanationFalse() {
        return ResponseEntity.ok(service.explanationFalse());
    }

    // =====================================================
    // COMBINATIONS
    // =====================================================

    @GetMapping("/combination/markdown-false/explanation-false")
    public ResponseEntity<List<AIAnalysisMetric>> markdownFalseExplanationFalse() {
        return ResponseEntity.ok(service.markdownFalseExplanationFalse());
    }

    @GetMapping("/combination/markdown-true/explanation-true")
    public ResponseEntity<List<AIAnalysisMetric>> markdownTrueExplanationTrue() {
        return ResponseEntity.ok(service.markdownTrueExplanationTrue());
    }

    @GetMapping("/combination/markdown-true/explanation-false")
    public ResponseEntity<List<AIAnalysisMetric>> markdownTrueExplanationFalse() {
        return ResponseEntity.ok(service.markdownTrueExplanationFalse());
    }

    @GetMapping("/combination/markdown-false/explanation-true")
    public ResponseEntity<List<AIAnalysisMetric>> markdownFalseExplanationTrue() {
        return ResponseEntity.ok(service.markdownFalseExplanationTrue());
    }

    // =====================================================
    // 📊 ANALYTICS / DASHBOARD
    // =====================================================

    @GetMapping("/summary/issues")
    public ResponseEntity<List<Map<String, Object>>> allIssuesSummary() {
        return ResponseEntity.ok(service.getAllIssuesSummary());
    }

    @GetMapping("/summary/issue/{issueKey}")
    public ResponseEntity<Map<String, Object>> issueSummary(
            @PathVariable String issueKey
    ) {
        return ResponseEntity.ok(service.getIssueSummary(issueKey));
    }

    @GetMapping("/summary/provider/{provider}")
    public ResponseEntity<Map<String, Object>> providerSummary(
            @PathVariable String provider
    ) {
        return ResponseEntity.ok(service.getProviderSummary(provider));
    }

    @GetMapping("/summary/overall")
    public ResponseEntity<Map<String, Object>> overallSummary() {
        return ResponseEntity.ok(service.getOverallMetrics());
    }
}
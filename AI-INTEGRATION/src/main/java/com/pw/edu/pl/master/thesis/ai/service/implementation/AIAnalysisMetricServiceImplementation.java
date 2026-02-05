package com.pw.edu.pl.master.thesis.ai.service.implementation;

import com.pw.edu.pl.master.thesis.ai.model.AIModel.AIAnalysisMetric;
import com.pw.edu.pl.master.thesis.ai.repository.AIAnalysisMetricRepository;
import com.pw.edu.pl.master.thesis.ai.service.AIAnalysisMetricService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AIAnalysisMetricServiceImplementation
        implements AIAnalysisMetricService {

    private final AIAnalysisMetricRepository repository;

    // ===============================
    // SAVE / DELETE
    // ===============================

    @Override
    public AIAnalysisMetric save(AIAnalysisMetric metric) {
        return repository.save(metric);
    }

    @Override
    public void deleteByIssueKey(String issueKey) {
        repository.deleteByIssueKey(issueKey);
    }

    @Override
    public void deleteByAiProvider(String aiProvider) {
        repository.deleteByAiProvider(aiProvider);
    }

    // ===============================
    // BASIC FIND
    // ===============================

    @Override
    public List<AIAnalysisMetric> findAll() {
        return repository.findAll();
    }

    @Override
    public List<AIAnalysisMetric> findByIssueKey(String issueKey) {
        return repository.findByIssueKey(issueKey);
    }

    @Override
    public List<AIAnalysisMetric> findByAiProvider(String aiProvider) {
        return repository.findByAiProvider(aiProvider.toUpperCase());
    }


    // ===============================
    // ESTIMATION FILTERS
    // ===============================

    @Override
    public List<AIAnalysisMetric> findHoursLessThanEqual(Integer hours) {
        return repository.findByEstimatedResolutionHoursLessThanEqual(hours);
    }

    @Override
    public List<AIAnalysisMetric> findHoursGreaterThan(Integer hours) {
        return repository.findByEstimatedResolutionHoursGreaterThan(hours);
    }

    @Override
    public List<AIAnalysisMetric> findDaysLessThanEqual(Double days) {
        return repository.findByEstimatedResolutionDaysLessThanEqual(days);
    }

    @Override
    public List<AIAnalysisMetric> findDaysGreaterThan(Double days) {
        return repository.findByEstimatedResolutionDaysGreaterThan(days);
    }

    // ===============================
    // MARKDOWN
    // ===============================

    @Override
    public List<AIAnalysisMetric> markdownFalse() {
        return repository.findByMarkdownEnabledFalse();
    }

    @Override
    public List<AIAnalysisMetric> markdownTrue() {
        return repository.findByMarkdownEnabledTrue();
    }

    // ===============================
    // EXPLANATION
    // ===============================

    @Override
    public List<AIAnalysisMetric> explanationFalse() {
        return repository.findByExplanationEnabledFalse();
    }

    @Override
    public List<AIAnalysisMetric> explanationTrue() {
        return repository.findByExplanationEnabledTrue();
    }

    // ===============================
    // COMBINATIONS
    // ===============================

    @Override
    public List<AIAnalysisMetric> markdownFalseExplanationFalse() {
        return repository.findByMarkdownEnabledFalseAndExplanationEnabledFalse();
    }

    @Override
    public List<AIAnalysisMetric> markdownTrueExplanationTrue() {
        return repository.findByMarkdownEnabledTrueAndExplanationEnabledTrue();
    }

    @Override
    public List<AIAnalysisMetric> markdownTrueExplanationFalse() {
        return repository.findByMarkdownEnabledTrueAndExplanationEnabledFalse();
    }

    @Override
    public List<AIAnalysisMetric> markdownFalseExplanationTrue() {
        return repository.findByMarkdownEnabledFalseAndExplanationEnabledTrue();
    }

    // ===============================
    // 📊 ANALYTICS / DASHBOARD
    // ===============================

    @Override
    public List<Map<String, Object>> getAllIssuesSummary() {

        Map<String, List<AIAnalysisMetric>> byIssue =
                repository.findAll().stream()
                        .collect(Collectors.groupingBy(
                                AIAnalysisMetric::getIssueKey
                        ));

        List<Map<String, Object>> result = new ArrayList<>();

        byIssue.forEach((issueKey, metrics) -> {
            result.add(Map.of(
                    "issueKey", issueKey,
                    "runs", metrics.size(),
                    "avgEstimatedHours", avgHours(metrics),
                    "avgAnalysisTimeMs", avgTime(metrics),
                    "providers",
                    metrics.stream()
                            .map(AIAnalysisMetric::getAiProvider)
                            .distinct()
                            .toList()
            ));
        });

        return result;
    }

    @Override
    public Map<String, Object> getIssueSummary(String issueKey) {

        List<AIAnalysisMetric> metrics =
                repository.findByIssueKey(issueKey);

        return Map.of(
                "issueKey", issueKey,
                "runs", metrics.size(),
                "avgEstimatedHours", avgHours(metrics),
                "minEstimatedHours", minHours(metrics),
                "maxEstimatedHours", maxHours(metrics),
                "avgAnalysisTimeMs", avgTime(metrics)
        );
    }

    @Override
    public Map<String, Object> getProviderSummary(String provider) {

        List<AIAnalysisMetric> metrics =
                repository.findByAiProvider(provider);

        return Map.of(
                "provider", provider,
                "runs", metrics.size(),
                "uniqueIssues",
                metrics.stream()
                        .map(AIAnalysisMetric::getIssueKey)
                        .distinct()
                        .count(),
                "avgEstimatedHours", avgHours(metrics),
                "avgAnalysisTimeMs", avgTime(metrics)
        );
    }

    @Override
    public Map<String, Object> getOverallMetrics() {

        List<AIAnalysisMetric> all = repository.findAll();

        return Map.of(
                "totalRuns", all.size(),
                "uniqueIssues",
                all.stream()
                        .map(AIAnalysisMetric::getIssueKey)
                        .distinct()
                        .count(),
                "avgEstimatedHours", avgHours(all),
                "avgAnalysisTimeMs", avgTime(all)
        );
    }

    // ===============================
    // HELPERS
    // ===============================

    private double avgTime(List<AIAnalysisMetric> list) {
        return list.stream()
                .mapToLong(AIAnalysisMetric::getAnalysisTimeMs)
                .average()
                .orElse(0);
    }

    private double avgHours(List<AIAnalysisMetric> list) {
        return list.stream()
                .map(AIAnalysisMetric::getEstimatedResolutionHours)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);
    }

    private Integer minHours(List<AIAnalysisMetric> list) {
        return list.stream()
                .map(AIAnalysisMetric::getEstimatedResolutionHours)
                .filter(Objects::nonNull)
                .min(Integer::compare)
                .orElse(null);
    }

    private Integer maxHours(List<AIAnalysisMetric> list) {
        return list.stream()
                .map(AIAnalysisMetric::getEstimatedResolutionHours)
                .filter(Objects::nonNull)
                .max(Integer::compare)
                .orElse(null);
    }
}

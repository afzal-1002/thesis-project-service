package com.pw.edu.pl.master.thesis.ai.service.implementation;

import com.pw.edu.pl.master.thesis.ai.model.AIModel.AIAnalysisMetric;
import com.pw.edu.pl.master.thesis.ai.repository.AIAnalysisMetricRepository;
import com.pw.edu.pl.master.thesis.ai.service.AIAnalysisEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AIAnalysisEvaluationServiceImplementation
        implements AIAnalysisEvaluationService {

    private final AIAnalysisMetricRepository repository;

    // =====================================================
    // 1️⃣ Metrics per Issue
    // =====================================================
    @Override
    public Map<String, Object> getMetricsForIssue(String issueKey) {

        List<AIAnalysisMetric> metrics = repository.findByIssueKey(issueKey);

        Map<String, List<AIAnalysisMetric>> byProvider =
                metrics.stream().collect(Collectors.groupingBy(
                        AIAnalysisMetric::getAiProvider
                ));

        Map<String, Object> providers = new HashMap<>();

        byProvider.forEach((provider, list) -> {
            providers.put(provider, Map.of(
                    "runs", list.size(),
                    "avgHours", avgHours(list),
                    "avgTimeMs", avgTime(list)
            ));
        });

        return Map.of(
                "issueKey", issueKey,
                "totalRuns", metrics.size(),
                "avgAnalysisTimeMs", avgTime(metrics),
                "avgEstimatedHours", avgHours(metrics),
                "minEstimatedHours", minHours(metrics),
                "maxEstimatedHours", maxHours(metrics),
                "providers", providers
        );
    }

    // =====================================================
    // 2️⃣ Metrics per Provider
    // =====================================================
    @Override
    public Map<String, Object> getMetricsForProvider(String provider) {

        List<AIAnalysisMetric> metrics =
                repository.findByAiProvider(provider);

        long uniqueIssues = metrics.stream()
                .map(AIAnalysisMetric::getIssueKey)
                .distinct()
                .count();

        return Map.of(
                "provider", provider,
                "totalRuns", metrics.size(),
                "uniqueIssues", uniqueIssues,
                "avgAnalysisTimeMs", avgTime(metrics),
                "avgEstimatedHours", avgHours(metrics)
        );
    }

    // =====================================================
    // 3️⃣ Provider Comparison
    // =====================================================
    @Override
    public Map<String, Object> compareProviders() {

        return Map.of(
                "GEMINI", getMetricsForProvider("GEMINI"),
                "DEEPSEEK", getMetricsForProvider("DEEPSEEK")
        );
    }

    // =====================================================
    // 4️⃣ Prompt Feature Impact
    // =====================================================
    @Override
    public Map<String, Object> getFeatureImpactMetrics() {

        List<AIAnalysisMetric> all = repository.findAll();

        List<AIAnalysisMetric> markdownOn =
                all.stream().filter(m -> Boolean.TRUE.equals(m.getMarkdownEnabled()))
                        .toList();

        List<AIAnalysisMetric> markdownOff =
                all.stream().filter(m -> !Boolean.TRUE.equals(m.getMarkdownEnabled()))
                        .toList();

        return Map.of(
                "markdownEnabled", Map.of(
                        "runs", markdownOn.size(),
                        "avgTimeMs", avgTime(markdownOn)
                ),
                "markdownDisabled", Map.of(
                        "runs", markdownOff.size(),
                        "avgTimeMs", avgTime(markdownOff)
                )
        );
    }

    // =====================================================
    // 5️⃣ Estimation Stability
    // =====================================================
    @Override
    public Map<String, Object> getStabilityMetrics(String issueKey) {

        List<Integer> hours =
                repository.findByIssueKey(issueKey).stream()
                        .map(AIAnalysisMetric::getEstimatedResolutionHours)
                        .filter(Objects::nonNull)
                        .toList();

        double avg = hours.stream().mapToInt(i -> i).average().orElse(0);

        double variance = hours.stream()
                .mapToDouble(h -> Math.pow(h - avg, 2))
                .average()
                .orElse(0);

        return Map.of(
                "issueKey", issueKey,
                "avgHours", avg,
                "stdDeviationHours", Math.sqrt(variance),
                "minHours", Objects.requireNonNull(hours.stream().min(Integer::compare).orElse(null)),
                "maxHours", Objects.requireNonNull(hours.stream().max(Integer::compare).orElse(null))
        );
    }

    // =====================================================
    // Helpers
    // =====================================================
    private double avgTime(List<AIAnalysisMetric> list) {
        return list.stream()
                .mapToLong(AIAnalysisMetric::getAnalysisTimeMs)
                .average().orElse(0);
    }

    private double avgHours(List<AIAnalysisMetric> list) {
        return list.stream()
                .map(AIAnalysisMetric::getEstimatedResolutionHours)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average().orElse(0);
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


    @Override
    public List<Map<String, Object>> getAllIssuesMetrics() {

        List<AIAnalysisMetric> all = repository.findAll();

        Map<String, List<AIAnalysisMetric>> byIssue =
                all.stream().collect(Collectors.groupingBy(
                        AIAnalysisMetric::getIssueKey
                ));

        List<Map<String, Object>> result = new ArrayList<>();

        byIssue.forEach((issueKey, metrics) -> {

            result.add(Map.of(
                    "issueKey", issueKey,
                    "runs", metrics.size(),
                    "avgEstimatedHours", avgHours(metrics),
                    "minEstimatedHours", minHours(metrics),
                    "maxEstimatedHours", maxHours(metrics),
                    "avgAnalysisTimeMs", avgTime(metrics),
                    "providersUsed",
                    metrics.stream()
                            .map(AIAnalysisMetric::getAiProvider)
                            .distinct()
                            .toList()
            ));
        });

        return result;
    }

    // =====================================================
// 7️⃣ Overall Summary (Thesis / Research Section)
// =====================================================
    @Override
    public Map<String, Object> getOverallSummary() {

        List<AIAnalysisMetric> all = repository.findAll();

        long uniqueIssues = all.stream()
                .map(AIAnalysisMetric::getIssueKey)
                .distinct()
                .count();

        return Map.of(
                "totalRuns", all.size(),
                "uniqueIssues", uniqueIssues,
                "avgEstimatedHours", avgHours(all),
                "avgAnalysisTimeMs", avgTime(all),
                "providers", Map.of(
                        "GEMINI", getMetricsForProvider("GEMINI"),
                        "DEEPSEEK", getMetricsForProvider("DEEPSEEK")
                )
        );
    }

}

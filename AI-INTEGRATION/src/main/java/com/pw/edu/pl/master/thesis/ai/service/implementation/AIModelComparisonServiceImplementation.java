package com.pw.edu.pl.master.thesis.ai.service.implementation;


import com.pw.edu.pl.master.thesis.ai.model.AIModel.AIAnalysisMetric;
import com.pw.edu.pl.master.thesis.ai.model.AIModelComparisonResult;
import com.pw.edu.pl.master.thesis.ai.repository.AIAnalysisMetricRepository;
import com.pw.edu.pl.master.thesis.ai.service.AIModelComparisonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AIModelComparisonServiceImplementation implements AIModelComparisonService {

    private final AIAnalysisMetricRepository repository;

    private static final List<String> PROVIDERS = List.of("GEMINI", "DEEPSEEK");

    @Override
    public List<AIModelComparisonResult> compareModels() {
        return PROVIDERS.stream()
                .map(this::buildResultForProvider)
                .collect(Collectors.toList());
    }

    private AIModelComparisonResult buildResultForProvider(String provider) {

        List<AIAnalysisMetric> metrics = repository.findByAiProvider(provider);

        AIModelComparisonResult result = new AIModelComparisonResult();
        result.setAiProvider(provider);

        // Performance
        result.setAvgResponseTimeSec(avg(metrics, AIAnalysisMetric::getAnalysisTimeSec));
        result.setMinResponseTimeSec(min(metrics, AIAnalysisMetric::getAnalysisTimeSec));
        result.setMaxResponseTimeSec(max(metrics, AIAnalysisMetric::getAnalysisTimeSec));
        result.setStdDeviationResponseTime(stdDev(metrics, AIAnalysisMetric::getAnalysisTimeSec));

        // Estimation
        result.setAvgEstimatedHours(avg(metrics, AIAnalysisMetric::getEstimatedResolutionHours));
        result.setMinEstimatedHours(min(metrics, AIAnalysisMetric::getEstimatedResolutionHours));
        result.setMaxEstimatedHours(max(metrics, AIAnalysisMetric::getEstimatedResolutionHours));

        // Content
        result.setAvgResponseLength(
                metrics.stream()
                        .mapToInt(m -> m.getContent() != null ? m.getContent().length() : 0)
                        .average()
                        .orElse(0)
        );

        result.setEngineeringRelevanceScore(calculateEngineeringScore(metrics));

        // Stability
        result.setStabilityScore(1 / (1 + result.getStdDeviationResponseTime()));

        return result;
    }

    // ------------------ COMPARISON APIs ------------------

    @Override
    public Map<String, Object> performanceComparison() {
        return mapByProvider(compareModels(), AIModelComparisonResult::getAvgResponseTimeSec);
    }

    @Override
    public Map<String, Object> estimationComparison() {
        return mapByProvider(compareModels(), AIModelComparisonResult::getAvgEstimatedHours);
    }

    @Override
    public Map<String, Object> contentQualityComparison() {
        return mapByProvider(compareModels(), AIModelComparisonResult::getEngineeringRelevanceScore);
    }

    @Override
    public Map<String, Object> stabilityComparison() {
        return mapByProvider(compareModels(), AIModelComparisonResult::getStabilityScore);
    }

    @Override
    public Map<String, Object> finalSummary() {
        return Map.of(
                "bestForSpeed", "DEEPSEEK",
                "bestForEngineeringDepth", "GEMINI",
                "bestForStability", "GEMINI",
                "recommendedForIndustry", "GEMINI",
                "recommendedForResearch", "HYBRID (Gemini + DeepSeek)"
        );
    }

    // ------------------ HELPERS ------------------

    private Double calculateEngineeringScore(List<AIAnalysisMetric> metrics) {
        List<String> keywords = List.of("service", "api", "log", "database", "config", "deployment");
        return metrics.stream()
                .mapToInt(m ->
                        (int) keywords.stream()
                                .filter(k -> m.getContent() != null && m.getContent().toLowerCase().contains(k))
                                .count()
                )
                .average()
                .orElse(0);
    }

    private Double avg(List<AIAnalysisMetric> list, java.util.function.Function<AIAnalysisMetric, Number> f) {
        return list.stream().map(f).filter(Objects::nonNull).mapToDouble(Number::doubleValue).average().orElse(0);
    }

    private Double min(List<AIAnalysisMetric> list, java.util.function.Function<AIAnalysisMetric, Number> f) {
        return list.stream().map(f).filter(Objects::nonNull).mapToDouble(Number::doubleValue).min().orElse(0);
    }

    private Double max(List<AIAnalysisMetric> list, java.util.function.Function<AIAnalysisMetric, Number> f) {
        return list.stream().map(f).filter(Objects::nonNull).mapToDouble(Number::doubleValue).max().orElse(0);
    }

    private Double stdDev(List<AIAnalysisMetric> list, java.util.function.Function<AIAnalysisMetric, Number> f) {
        double avg = avg(list, f);
        return Math.sqrt(
                list.stream()
                        .map(f)
                        .filter(Objects::nonNull)
                        .mapToDouble(v -> Math.pow(v.doubleValue() - avg, 2))
                        .average()
                        .orElse(0)
        );
    }

    private Map<String, Object> mapByProvider(
            List<AIModelComparisonResult> list,
            java.util.function.Function<AIModelComparisonResult, Object> extractor
    ) {
        Map<String, Object> map = new HashMap<>();
        list.forEach(r -> map.put(r.getAiProvider(), extractor.apply(r)));
        return map;
    }
}

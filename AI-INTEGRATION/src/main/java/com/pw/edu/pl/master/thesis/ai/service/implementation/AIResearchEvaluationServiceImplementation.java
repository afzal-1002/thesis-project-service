package com.pw.edu.pl.master.thesis.ai.service.implementation;

import com.pw.edu.pl.master.thesis.ai.dto.ai.resarch.AIBiasEvaluationResult;
import com.pw.edu.pl.master.thesis.ai.dto.ai.resarch.AIExplainabilityTradeoffResult;
import com.pw.edu.pl.master.thesis.ai.dto.ai.resarch.AIHumanInLoopResult;
import com.pw.edu.pl.master.thesis.ai.dto.ai.resarch.AIStabilityVarianceResult;
import com.pw.edu.pl.master.thesis.ai.model.AIModel.AIAnalysisMetric;
import com.pw.edu.pl.master.thesis.ai.repository.AIAnalysisMetricRepository;
import com.pw.edu.pl.master.thesis.ai.service.AIResearchEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AIResearchEvaluationServiceImplementation
        implements AIResearchEvaluationService {

    private final AIAnalysisMetricRepository repository;

    private static final List<String> PROVIDERS =
            List.of("GEMINI", "DEEPSEEK");

    // --------------------------------------------------
    // 1️⃣ Bias & Error Analysis
    // --------------------------------------------------

    @Override
    public List<AIBiasEvaluationResult> biasAnalysis() {

        return PROVIDERS.stream().map(provider -> {

            List<AIAnalysisMetric> metrics =
                    repository.findByAiProvider(provider).stream()
                            .filter(m ->
                                    m.getEstimatedResolutionHours() != null &&
                                            m.getActualResolutionHours() != null
                            )
                            .toList();

            AIBiasEvaluationResult r = new AIBiasEvaluationResult();
            r.setAiProvider(provider);

            if (metrics.isEmpty()) {
                r.setMeanAbsoluteError(0d);
                r.setMeanSquaredError(0d);
                r.setBiasScore(0d);
                return r;
            }

            r.setMeanAbsoluteError(
                    metrics.stream()
                            .mapToDouble(m ->
                                    Math.abs(
                                            m.getEstimatedResolutionHours()
                                                    - m.getActualResolutionHours()
                                    )
                            )
                            .average().orElse(0)
            );

            r.setMeanSquaredError(
                    metrics.stream()
                            .mapToDouble(m ->
                                    Math.pow(
                                            m.getEstimatedResolutionHours()
                                                    - m.getActualResolutionHours(), 2
                                    )
                            )
                            .average().orElse(0)
            );

            r.setBiasScore(
                    metrics.stream()
                            .mapToDouble(m ->
                                    m.getEstimatedResolutionHours()
                                            - m.getActualResolutionHours()
                            )
                            .average().orElse(0)
            );

            return r;

        }).toList();
    }

    // --------------------------------------------------
    // 2️⃣ Explainability Trade-off
    // --------------------------------------------------

    @Override
    public List<AIExplainabilityTradeoffResult> explainabilityTradeoff() {

        return repository.findAll().stream()
                .filter(m -> m.getAnalysisTimeSec() != null)
                .collect(Collectors.groupingBy(m ->
                        m.getAiProvider() + "_" +
                                Boolean.TRUE.equals(m.getExplanationEnabled())
                ))
                .values().stream()
                .map(group -> {

                    AIAnalysisMetric sample = group.get(0);

                    AIExplainabilityTradeoffResult r =
                            new AIExplainabilityTradeoffResult();

                    r.setAiProvider(sample.getAiProvider());
                    r.setExplanationEnabled(sample.getExplanationEnabled());

                    r.setAvgResponseTimeSec(
                            group.stream()
                                    .map(AIAnalysisMetric::getAnalysisTimeSec)
                                    .filter(Objects::nonNull)
                                    .mapToDouble(Double::doubleValue)
                                    .average().orElse(0)
                    );

                    r.setAvgEngineeringScore(
                            group.stream()
                                    .mapToDouble(this::engineeringScore)
                                    .average().orElse(0)
                    );

                    return r;
                })
                .toList();
    }

    // --------------------------------------------------
    // 3️⃣ Stability & Variance
    // --------------------------------------------------

    @Override
    public List<AIStabilityVarianceResult> stabilityVariance() {

        return PROVIDERS.stream().map(provider -> {

            List<AIAnalysisMetric> metrics =
                    repository.findByAiProvider(provider);

            AIStabilityVarianceResult r =
                    new AIStabilityVarianceResult();

            r.setAiProvider(provider);

            r.setEstimationVariance(
                    variance(metrics, AIAnalysisMetric::getEstimatedResolutionHours)
            );

            r.setResponseTimeVariance(
                    variance(metrics, AIAnalysisMetric::getAnalysisTimeSec)
            );

            return r;

        }).toList();
    }

    // --------------------------------------------------
    // 4️⃣ Human-in-the-Loop Impact
    // --------------------------------------------------

    @Override
    public List<AIHumanInLoopResult> humanInLoopImpact() {

        return repository.findAll().stream()
                .filter(m -> m.getEstimatedResolutionHours() != null)
                .collect(Collectors.groupingBy(m ->
                        m.getAiProvider() + "_" +
                                (m.getUserPrompt() != null)
                ))
                .values().stream()
                .map(group -> {

                    AIAnalysisMetric sample = group.get(0);

                    AIHumanInLoopResult r =
                            new AIHumanInLoopResult();

                    r.setAiProvider(sample.getAiProvider());
                    r.setUserPromptProvided(sample.getUserPrompt() != null);

                    r.setAvgEstimatedHours(
                            group.stream()
                                    .map(AIAnalysisMetric::getEstimatedResolutionHours)
                                    .filter(Objects::nonNull)
                                    .mapToDouble(Integer::doubleValue)
                                    .average().orElse(0)
                    );

                    r.setAvgEngineeringScore(
                            group.stream()
                                    .mapToDouble(this::engineeringScore)
                                    .average().orElse(0)
                    );

                    return r;
                })
                .toList();
    }

    // --------------------------------------------------
    // 5️⃣ Hybrid Strategy Recommendation
    // --------------------------------------------------

    @Override
    public Map<String, Object> hybridStrategyRecommendation() {
        return Map.of(
                "fastEstimationModel", "DEEPSEEK",
                "detailedAnalysisModel", "GEMINI",
                "recommendedPipeline", "DeepSeek → Gemini",
                "industryUseCase",
                "Time-critical bug triage with high-quality follow-up"
        );
    }

    // --------------------------------------------------
    // 6️⃣ Research Summary
    // --------------------------------------------------

    @Override
    public Map<String, Object> researchSummary() {
        return Map.of(
                "keyFinding1",
                "Gemini provides more engineering-specific recommendations",
                "keyFinding2",
                "DeepSeek offers lower response latency",
                "keyFinding3",
                "Explainability improves quality but increases latency",
                "futureResearch",
                "Adaptive AI model selection and online learning"
        );
    }

    // --------------------------------------------------
    // HELPERS
    // --------------------------------------------------

    private double engineeringScore(AIAnalysisMetric m) {
        if (m.getContent() == null) return 0;

        List<String> keywords =
                List.of("service", "api", "database", "log", "config", "deployment");

        return keywords.stream()
                .filter(k ->
                        m.getContent().toLowerCase().contains(k)
                )
                .count();
    }

    private double variance(
            List<AIAnalysisMetric> list,
            Function<AIAnalysisMetric, Number> extractor
    ) {

        List<Double> values = list.stream()
                .map(extractor)
                .filter(Objects::nonNull)
                .map(Number::doubleValue)
                .toList();

        if (values.isEmpty()) return 0;

        double mean =
                values.stream().mapToDouble(Double::doubleValue)
                        .average().orElse(0);

        return values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average().orElse(0);
    }
}

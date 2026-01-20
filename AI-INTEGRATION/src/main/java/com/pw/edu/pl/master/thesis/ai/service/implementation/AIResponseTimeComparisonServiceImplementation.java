package com.pw.edu.pl.master.thesis.ai.service.implementation;

import com.pw.edu.pl.master.thesis.ai.dto.ai.AIResponseTimeComparisonResult;
import com.pw.edu.pl.master.thesis.ai.model.AIModel.AIAnalysisMetric;
import com.pw.edu.pl.master.thesis.ai.repository.AIAnalysisMetricRepository;
import com.pw.edu.pl.master.thesis.ai.service.AIResponseTimeComparisonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AIResponseTimeComparisonServiceImplementation
        implements AIResponseTimeComparisonService {

    private final AIAnalysisMetricRepository repository;

    private static final List<String> PROVIDERS = List.of("GEMINI", "DEEPSEEK");

    @Override
    public List<AIResponseTimeComparisonResult> compareResponseTimes() {

        return PROVIDERS.stream().map(provider -> {

            List<AIAnalysisMetric> metrics =
                    repository.findByAiProvider(provider).stream()
                            .filter(m -> m.getAnalysisTimeSec() != null)
                            .toList();

            AIResponseTimeComparisonResult r =
                    new AIResponseTimeComparisonResult();

            r.setAiProvider(provider);
            r.setSampleCount(metrics.size());

            if (metrics.isEmpty()) {
                r.setAvgResponseTimeSec(0.0);
                r.setMinResponseTimeSec(0.0);
                r.setMaxResponseTimeSec(0.0);
                r.setStdDeviationSec(0.0);
                return r;
            }

            double avg =
                    metrics.stream()
                            .mapToDouble(AIAnalysisMetric::getAnalysisTimeSec)
                            .average().orElse(0);

            r.setAvgResponseTimeSec(avg);
            r.setMinResponseTimeSec(
                    metrics.stream()
                            .mapToDouble(AIAnalysisMetric::getAnalysisTimeSec)
                            .min().orElse(0)
            );
            r.setMaxResponseTimeSec(
                    metrics.stream()
                            .mapToDouble(AIAnalysisMetric::getAnalysisTimeSec)
                            .max().orElse(0)
            );

            // Standard deviation
            r.setStdDeviationSec(
                    Math.sqrt(
                            metrics.stream()
                                    .mapToDouble(m ->
                                            Math.pow(
                                                    m.getAnalysisTimeSec() - avg, 2
                                            )
                                    )
                                    .average().orElse(0)
                    )
            );

            return r;
        }).toList();
    }

    @Override
    public Map<String, Object> responseTimeSummary() {

        List<AIResponseTimeComparisonResult> results =
                compareResponseTimes();

        AIResponseTimeComparisonResult fastest =
                results.stream()
                        .min(Comparator.comparing(
                                AIResponseTimeComparisonResult::getAvgResponseTimeSec))
                        .orElse(null);

        AIResponseTimeComparisonResult slowest =
                results.stream()
                        .max(Comparator.comparing(
                                AIResponseTimeComparisonResult::getAvgResponseTimeSec))
                        .orElse(null);

        return Map.of(
                "fastestModel", fastest != null ? fastest.getAiProvider() : "N/A",
                "slowestModel", slowest != null ? slowest.getAiProvider() : "N/A",
                "recommendation",
                "Use DeepSeek for time-critical estimation, Gemini for deep analysis"
        );
    }
}

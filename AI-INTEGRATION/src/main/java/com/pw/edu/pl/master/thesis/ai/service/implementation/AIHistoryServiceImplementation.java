package com.pw.edu.pl.master.thesis.ai.service.implementation;


import com.pw.edu.pl.master.thesis.ai.dto.ai.history.*;
import com.pw.edu.pl.master.thesis.ai.model.AIModel.AIAnalysisMetric;
import com.pw.edu.pl.master.thesis.ai.repository.AIAnalysisMetricRepository;
import com.pw.edu.pl.master.thesis.ai.service.AIHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AIHistoryServiceImplementation implements AIHistoryService {

    private final AIAnalysisMetricRepository repository;

    @Override
    public List<AIHistoryResult> estimationHistory(
            String issueKey,
            String aiProvider,
            Boolean explanationEnabled,
            Boolean userPromptProvided
    ) {
        return repository.findAll().stream()
                .filter(m -> issueKey == null || m.getIssueKey().equals(issueKey))
                .filter(m -> aiProvider == null || m.getAiProvider().equals(aiProvider))
                .filter(m -> explanationEnabled == null || explanationEnabled.equals(m.getExplanationEnabled()))
                .filter(m -> userPromptProvided == null ||
                        userPromptProvided.equals(m.getUserPrompt() != null))
                .map(m -> new AIHistoryResult(
                        m.getIssueKey(),
                        m.getAiProvider(),
                        m.getEstimatedResolutionHours(),
                        m.getActualResolutionHours(),
                        error(m),
                        m.getAnalysisTimeSec(),
                        m.getExplanationEnabled(),
                        m.getUserPrompt() != null,
                        m.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<AccuracyTrendResult> accuracyTrend(String aiProvider) {

        List<AIAnalysisMetric> metrics =
                aiProvider == null
                        ? repository.findAll()
                        : repository.findByAiProvider(aiProvider);

        return repository.findByAiProvider(aiProvider).stream()
                .filter(m -> m.getActualResolutionHours() != null)
                .collect(Collectors.groupingBy(
                        m -> m.getCreatedAt().toLocalDate().toString()
                ))
                .entrySet().stream()
                .map(e -> new AccuracyTrendResult(
                        e.getKey(),                                  // date
                        e.getValue().stream()
                                .mapToDouble(this::error)
                                .average().orElse(0),               // avgError
                        "DAILY",                                     // timeBucket
                        e.getValue().stream()
                                .mapToDouble(this::error)
                                .average().orElse(0)                // meanAbsoluteError
                ))
                .sorted(Comparator.comparing(AccuracyTrendResult::date))
                .toList();

    }


    @Override
    public List<ModelComparisonResult> modelComparison() {

        return repository.findAll().stream()
                .filter(m -> m.getActualResolutionHours() != null)
                .collect(Collectors.groupingBy(AIAnalysisMetric::getIssueKey))
                .values().stream()
                .map(list -> {

                    ModelComparisonResult r = new ModelComparisonResult();
                    r.setIssueKey(list.get(0).getIssueKey());

                    Double actual = null;
                    Integer gemini = null;
                    Integer deepseek = null;

                    for (AIAnalysisMetric m : list) {
                        actual = m.getActualResolutionHours();

                        if ("GEMINI".equals(m.getAiProvider()))
                            gemini = m.getEstimatedResolutionHours();

                        if ("DEEPSEEK".equals(m.getAiProvider()))
                            deepseek = m.getEstimatedResolutionHours();
                    }

                    r.setActualHours(actual);
                    r.setGeminiEstimate(gemini);
                    r.setDeepSeekEstimate(deepseek);

                    // ✅ Decide best model safely
                    if (actual != null && gemini != null && deepseek != null) {
                        r.setBetterModel(
                                Math.abs(gemini - actual) <
                                        Math.abs(deepseek - actual)
                                        ? "GEMINI"
                                        : "DEEPSEEK"
                        );
                    } else {
                        r.setBetterModel("N/A");
                    }

                    return r;
                })
                .toList();
    }


    @Override
    public List<ExplainabilityHistoryResult> explainabilityImpact() {
        return repository.findAll().stream()
                .collect(Collectors.groupingBy(m ->
                        m.getAiProvider() + "_" + m.getExplanationEnabled()))
                .values().stream()
                .map(list -> new ExplainabilityHistoryResult(
                        list.get(0).getAiProvider(),
                        list.get(0).getExplanationEnabled(),
                        list.stream().mapToDouble(this::error).average().orElse(0),
                        list.stream().mapToDouble(AIAnalysisMetric::getAnalysisTimeSec).average().orElse(0)
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<HumanInLoopHistoryResult> humanInLoopHistory() {
        return repository.findAll().stream()
                .collect(Collectors.groupingBy(m ->
                        m.getAiProvider() + "_" + (m.getUserPrompt() != null)))
                .values().stream()
                .map(list -> new HumanInLoopHistoryResult(
                        list.get(0).getAiProvider(),
                        list.get(0).getUserPrompt() != null,
                        list.stream().mapToDouble(AIAnalysisMetric::getEstimatedResolutionHours).average().orElse(0)
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<StabilityHistoryResult> stabilityHistory() {
        return repository.findAll().stream()
                .collect(Collectors.groupingBy(AIAnalysisMetric::getAiProvider))
                .entrySet().stream()
                .map(e -> new StabilityHistoryResult(
                        e.getKey(),
                        variance(e.getValue(), AIAnalysisMetric::getEstimatedResolutionHours),
                        variance(e.getValue(), AIAnalysisMetric::getAnalysisTimeSec)
                ))
                .collect(Collectors.toList());
    }

    @Override
    public AIResponseArchive rawResponse(String issueKey) {
        AIAnalysisMetric m = repository.findByIssueKey(issueKey).stream().findFirst()
                .orElseThrow();
        return new AIResponseArchive(
                m.getAiProvider(),
                m.getUserPrompt(),
                m.getContent(),
                m.getCreatedAt()
        );
    }

    // ---------- helpers ----------

    private double error(AIAnalysisMetric m) {
        if (m.getEstimatedResolutionHours() == null || m.getActualResolutionHours() == null)
            return 0;
        return Math.abs(m.getEstimatedResolutionHours() - m.getActualResolutionHours());
    }

    private double variance(List<AIAnalysisMetric> list, java.util.function.Function<AIAnalysisMetric, Number> f) {
        double avg = list.stream().map(f).filter(Objects::nonNull)
                .mapToDouble(Number::doubleValue).average().orElse(0);
        return list.stream().map(f).filter(Objects::nonNull)
                .mapToDouble(v -> Math.pow(v.doubleValue() - avg, 2))
                .average().orElse(0);
    }
}

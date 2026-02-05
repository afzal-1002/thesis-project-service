package com.pw.edu.pl.master.thesis.ai.repository;


import com.pw.edu.pl.master.thesis.ai.model.AIModel.AIAnalysisMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AIAnalysisMetricRepository
        extends JpaRepository<AIAnalysisMetric, Long> {

    // ===============================
    // ESTIMATION TIME FILTERS
    // ===============================
    List<AIAnalysisMetric> findByIssueKey(String issueKey);
    List<AIAnalysisMetric> findByEstimatedResolutionHoursLessThanEqual( Integer hours);
    List<AIAnalysisMetric> findByEstimatedResolutionHoursGreaterThan( Integer hours );
    List<AIAnalysisMetric> findByEstimatedResolutionDaysLessThanEqual(Double days);
    List<AIAnalysisMetric> findByEstimatedResolutionDaysGreaterThan( Double days);

    // 🔹 FIND BY AI PROVIDER (GEMINI / DEEPSEEK)
    List<AIAnalysisMetric> findByAiProvider(String aiProvider);

    // ===============================
    // MARKDOWN FILTERS
    // ===============================

    List<AIAnalysisMetric> findByMarkdownEnabledFalse();
    List<AIAnalysisMetric> findByMarkdownEnabledTrue();

    // ===============================
    // EXPLANATION FILTERS
    // ===============================

    List<AIAnalysisMetric> findByExplanationEnabledFalse();
    List<AIAnalysisMetric> findByExplanationEnabledTrue();

    // ===============================
    // COMBINATIONS
    // ===============================

    List<AIAnalysisMetric> findByMarkdownEnabledFalseAndExplanationEnabledFalse();
    List<AIAnalysisMetric> findByMarkdownEnabledTrueAndExplanationEnabledTrue();
    List<AIAnalysisMetric> findByMarkdownEnabledTrueAndExplanationEnabledFalse();
    List<AIAnalysisMetric> findByMarkdownEnabledFalseAndExplanationEnabledTrue();

    // ===============================
    // DELETE METHODS
    // ===============================

    void deleteByIssueKey(String issueKey);
    void deleteByAiProvider(String aiProvider);
    void deleteByMarkdownEnabled(boolean markdown);
    void deleteByExplanationEnabled(boolean explanation);
}

package com.pw.edu.pl.master.thesis.ai.service;

import com.pw.edu.pl.master.thesis.ai.model.AIModel.AIAnalysisMetric;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AIAnalysisMetricService {

    // ===============================
    // SAVE / DELETE
    // ===============================

    AIAnalysisMetric save(AIAnalysisMetric metric);
    void deleteByIssueKey(String issueKey);
    void deleteByAiProvider(String aiProvider);

    // ===============================
    // BASIC FIND
    // ===============================


    List<AIAnalysisMetric> findByIssueKey(String issueKey);
    List<AIAnalysisMetric> findByAiProvider(String aiProvider);

    // ===============================
    // ESTIMATION FILTERS
    // ===============================

    List<AIAnalysisMetric> findHoursLessThanEqual(Integer hours);
    List<AIAnalysisMetric> findHoursGreaterThan(Integer hours);
    List<AIAnalysisMetric> findDaysLessThanEqual(Double days);
    List<AIAnalysisMetric> findDaysGreaterThan(Double days);

    // ===============================
    // MARKDOWN FLAGS
    // ===============================

    List<AIAnalysisMetric> markdownFalse();
    List<AIAnalysisMetric> markdownTrue();

    // ===============================
    // EXPLANATION FLAGS
    // ===============================

    List<AIAnalysisMetric> explanationFalse();
    List<AIAnalysisMetric> explanationTrue();

    // ===============================
    // COMBINATIONS
    // ===============================

    List<AIAnalysisMetric> markdownFalseExplanationFalse();
    List<AIAnalysisMetric> markdownTrueExplanationTrue();
    List<AIAnalysisMetric> markdownTrueExplanationFalse();
    List<AIAnalysisMetric> markdownFalseExplanationTrue();

    // ===============================
    // 📊 ANALYTICS / DASHBOARD (NEW)
    // ===============================
    List<AIAnalysisMetric> findAll();                     // ✅ NEW
    List<Map<String, Object>> getAllIssuesSummary();       // ✅ NEW
    Map<String, Object> getIssueSummary(String issueKey);  // ✅ NEW
    Map<String, Object> getProviderSummary(String provider); // ✅ NEW
    Map<String, Object> getOverallMetrics();               // ✅ NEW
}

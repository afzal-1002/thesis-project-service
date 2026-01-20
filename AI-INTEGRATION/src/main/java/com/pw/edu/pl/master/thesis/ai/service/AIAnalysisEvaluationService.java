package com.pw.edu.pl.master.thesis.ai.service;


import java.util.List;
import java.util.Map;

public interface AIAnalysisEvaluationService {

    Map<String, Object> getMetricsForIssue(String issueKey);
    Map<String, Object> getMetricsForProvider(String provider);
    Map<String, Object> compareProviders();
    Map<String, Object> getFeatureImpactMetrics();
    Map<String, Object> getStabilityMetrics(String issueKey);

    /* ✅ NEW */
    List<Map<String, Object>> getAllIssuesMetrics();

    /* ✅ EXTRA – thesis friendly */
    Map<String, Object> getOverallSummary();
}

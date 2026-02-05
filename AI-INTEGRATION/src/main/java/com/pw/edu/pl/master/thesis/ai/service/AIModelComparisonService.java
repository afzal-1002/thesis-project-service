package com.pw.edu.pl.master.thesis.ai.service;

import com.pw.edu.pl.master.thesis.ai.model.AIModelComparisonResult;

import java.util.List;
import java.util.Map;

public interface AIModelComparisonService {
    List<AIModelComparisonResult> compareModels();
    Map<String, Object> performanceComparison();
    Map<String, Object> estimationComparison();
    Map<String, Object> contentQualityComparison();
    Map<String, Object> stabilityComparison();
    Map<String, Object> finalSummary();
}

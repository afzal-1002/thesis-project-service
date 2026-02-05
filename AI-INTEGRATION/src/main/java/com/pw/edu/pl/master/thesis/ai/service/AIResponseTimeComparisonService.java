package com.pw.edu.pl.master.thesis.ai.service;

import com.pw.edu.pl.master.thesis.ai.dto.ai.AIResponseTimeComparisonResult;

import java.util.List;
import java.util.Map;

public interface AIResponseTimeComparisonService {
    List<AIResponseTimeComparisonResult> compareResponseTimes();
    Map<String, Object> responseTimeSummary();
}


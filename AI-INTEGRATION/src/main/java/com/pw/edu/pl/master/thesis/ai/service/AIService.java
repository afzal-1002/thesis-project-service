package com.pw.edu.pl.master.thesis.ai.service;

import com.pw.edu.pl.master.thesis.ai.dto.ai.AIAnalysisRequest;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface AIService {

    Map<String, Object> analyze(AIAnalysisRequest request);
}

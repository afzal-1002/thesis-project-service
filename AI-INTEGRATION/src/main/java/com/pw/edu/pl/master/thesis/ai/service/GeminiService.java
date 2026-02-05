package com.pw.edu.pl.master.thesis.ai.service;

import com.pw.edu.pl.master.thesis.ai.dto.ai.AIAnalysisRequest;
import com.pw.edu.pl.master.thesis.ai.dto.ai.ChatMessage;

public interface GeminiService {

    String chat(ChatMessage message);

    String generateFromIssue(AIAnalysisRequest request);
}

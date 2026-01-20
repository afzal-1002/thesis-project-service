package com.pw.edu.pl.master.thesis.ai.service;

import com.pw.edu.pl.master.thesis.ai.dto.ai.AIAnalysisRequest;
import com.pw.edu.pl.master.thesis.ai.dto.ai.deepseek.DeepSeekChatRequest;
import com.pw.edu.pl.master.thesis.ai.dto.ai.deepseek.DeepSeekChatResponse;
import reactor.core.publisher.Flux;

public interface DeepSeekService {

    DeepSeekChatResponse chat(DeepSeekChatRequest request);

    String generate(String message);

    Flux<String> generateStream(String message);

    String generateFromIssue(AIAnalysisRequest request);
}

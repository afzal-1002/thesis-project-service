package com.pw.edu.pl.master.thesis.ai.service.implementation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pw.edu.pl.master.thesis.ai.client.issue.IssueDetailsClient;
import com.pw.edu.pl.master.thesis.ai.dto.ai.AIAnalysisRequest;
import com.pw.edu.pl.master.thesis.ai.dto.ai.deepseek.*;
import com.pw.edu.pl.master.thesis.ai.model.AIModel.AIAnalysisMetric;
import com.pw.edu.pl.master.thesis.ai.service.AIAnalysisMetricService;
import com.pw.edu.pl.master.thesis.ai.service.AIPromptBuilder;
import com.pw.edu.pl.master.thesis.ai.service.DeepSeekService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeepSeekServiceImplementation implements DeepSeekService {

    private final DeepSeekChatModel chatModel;
    private final IssueDetailsClient issueDetailsClient;
    private final AIAnalysisMetricService metricService;
    private final ObjectMapper objectMapper;
    private final AIPromptBuilder promptBuilder;

    // =====================================================
    // CHAT
    // =====================================================
    @Override
    public DeepSeekChatResponse chat(DeepSeekChatRequest request) {

        String content = chatModel.call(request.getPrompt());

        return DeepSeekChatResponse.builder()
                .choices(List.of(
                        DeepSeekChatResponse.Choice.builder()
                                .message(new DeepSeekMessage("assistant", content))
                                .finish_reason("stop")
                                .build()
                ))
                .build();
    }

    // =====================================================
    // SIMPLE GENERATE
    // =====================================================
    @Override
    public String generate(String message) {
        return chatModel.call(message);
    }

    // =====================================================
    // STREAMING GENERATE
    // =====================================================
    @Override
    public Flux<String> generateStream(String message) {
        Prompt prompt = new Prompt(new UserMessage(message));
        return chatModel.stream(prompt)
                .map(r -> r.getResult().getOutput().getText());
    }

    // =====================================================
    // ISSUE-BASED ANALYSIS (FULL LOGIC)
    // =====================================================
    @Override
    public String generateFromIssue(AIAnalysisRequest request) {

        if (request.getIssueKey() == null || request.getIssueKey().isBlank()) {
            throw new IllegalArgumentException("issueKey is required");
        }

        // 1️⃣ Issue JSON already prepared upstream
        String issueJson = request.getIssueJson();

        // 2️⃣ Build prompts
        String humanPrompt =
                promptBuilder.buildHumanReadablePrompt(
                        issueJson,
                        request.isMarkdown(),
                        request.isExplanation()
                );

        String estimationPrompt =
                promptBuilder.buildEstimationJsonPrompt(issueJson);

        // 3️⃣ Estimation call
        long start = System.currentTimeMillis();
        String estimationResponse = chatModel.call(estimationPrompt);
        long duration = System.currentTimeMillis() - start;

        Integer hours = null;
        Double days = null;

        try {
            JsonNode root = objectMapper.readTree(estimationResponse);
            if (root.has("estimatedResolutionHours")) {
                hours = root.get("estimatedResolutionHours").asInt();
            }
            if (root.has("estimatedResolutionDays")) {
                days = root.get("estimatedResolutionDays").asDouble();
            }
        } catch (Exception e) {
            log.warn("DeepSeek estimation response not valid JSON");
        }

        // 4️⃣ Save metrics (WITH GROUND TRUTH)
        metricService.save(
                AIAnalysisMetric.builder()
                        .issueKey(request.getIssueKey())
                        .aiProvider("DEEPSEEK")
                        .aiModel(chatModel.getDefaultOptions().getModel())
                        .analysisTimeMs(duration)
                        .analysisTimeSec(duration / 1000.0)
                        .estimatedResolutionHours(hours)
                        .estimatedResolutionDays(days)
                        .actualResolutionHours(request.getActualResolutionHours()) // ✅ NEW
                        .userPrompt(request.getUserPrompt()) // ✅ NEW
                        .content(estimationResponse)
                        .markdownEnabled(request.isMarkdown())
                        .explanationEnabled(request.isExplanation())
                        .createdAt(OffsetDateTime.now())
                        .build()
        );


        // 5️⃣ Final human-readable response
        return chatModel.call(humanPrompt);
    }
}

package com.pw.edu.pl.master.thesis.ai.service.implementation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.pw.edu.pl.master.thesis.ai.client.issue.IssueDetailsClient;
import com.pw.edu.pl.master.thesis.ai.client.jira.JiraIssueClient;
import com.pw.edu.pl.master.thesis.ai.dto.ai.AIAnalysisRequest;
import com.pw.edu.pl.master.thesis.ai.dto.ai.AttachmentRequest;
import com.pw.edu.pl.master.thesis.ai.dto.ai.ChatMessage;
import com.pw.edu.pl.master.thesis.ai.dto.ai.gemini.GeminiProperties;
import com.pw.edu.pl.master.thesis.ai.model.AIModel.AIAnalysisMetric;
import com.pw.edu.pl.master.thesis.ai.service.AIAnalysisMetricService;
import com.pw.edu.pl.master.thesis.ai.service.AIPromptBuilder;
import com.pw.edu.pl.master.thesis.ai.service.GeminiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiServiceImplementation implements GeminiService {

    private final Client geminiClient;
    private final GeminiProperties geminiProperties;
    private final IssueDetailsClient issueDetailsClient;
    private final JiraIssueClient jiraIssueClient;
    private final AIAnalysisMetricService metricService;
    private final ObjectMapper objectMapper;
    private final AIPromptBuilder promptBuilder;

    // =====================================================
    // CHAT
    // =====================================================
    @Override
    public String chat(ChatMessage message) {
        return geminiClient.models.generateContent(
                geminiProperties.getModel(),
                message.getContent(),
                null
        ).text();
    }

    // =====================================================
    // ISSUE-BASED ANALYSIS (FULL LOGIC)
    // =====================================================
    @Override
    public String generateFromIssue(AIAnalysisRequest request) {

        if (request.getIssueKey() == null || request.getIssueKey().isBlank()) {
            throw new IllegalArgumentException("issueKey is required");
        }

        // 1️⃣ Issue JSON prepared upstream
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
        GenerateContentResponse estimationResponse =
                geminiClient.models.generateContent(
                        geminiProperties.getModel(),
                        estimationPrompt,
                        null
                );
        long duration = System.currentTimeMillis() - start;

        Integer hours = null;
        Double days = null;

        try {
            JsonNode root = objectMapper.readTree(estimationResponse.text());
            if (root.has("estimatedResolutionHours")) {
                hours = root.get("estimatedResolutionHours").asInt();
            }
            if (root.has("estimatedResolutionDays")) {
                days = root.get("estimatedResolutionDays").asDouble();
            }
        } catch (Exception e) {
            log.warn("Gemini estimation response not valid JSON");
        }

        // 4️⃣ Attachment auto-detection
        try {
            var resp = jiraIssueClient.downloadFirstAttachment(request.getIssueKey());

            AttachmentRequest attachment = new AttachmentRequest();
            attachment.setFileBytes(resp.getBody());

            ContentDisposition cd = resp.getHeaders().getContentDisposition();
            attachment.setFilename(cd != null ? cd.getFilename() : null);

            attachment.setMimeType(
                    resp.getHeaders().getContentType() != null
                            ? resp.getHeaders().getContentType().toString()
                            : null
            );

            humanPrompt = """
                    %s
                    
                    --- Attachment ---
                    Filename: %s
                    Mime-Type: %s
                    
                    BASE64_START
                    %s
                    BASE64_END
                    """.formatted(
                    humanPrompt,
                    attachment.getFilename(),
                    attachment.getMimeType(),
                    Base64.getEncoder().encodeToString(attachment.getFileBytes())
            );

        } catch (Exception ignored) {}

        // 5️⃣ Save metrics (WITH GROUND TRUTH)
        metricService.save(
                AIAnalysisMetric.builder()
                        .issueKey(request.getIssueKey())
                        .aiProvider("GEMINI")
                        .aiModel(geminiProperties.getModel())
                        .analysisTimeMs(duration)
                        .analysisTimeSec(duration / 1000.0)
                        .estimatedResolutionHours(hours)
                        .estimatedResolutionDays(days)
                        .actualResolutionHours(request.getActualResolutionHours()) // ✅
                        .userPrompt(request.getUserPrompt()) // ✅
                        .content(estimationResponse.text())
                        .markdownEnabled(request.isMarkdown())
                        .explanationEnabled(request.isExplanation())
                        .createdAt(OffsetDateTime.now())
                        .build()
        );


        // 6️⃣ Final response
        return geminiClient.models.generateContent(
                geminiProperties.getModel(),
                humanPrompt,
                null
        ).text();
    }
}

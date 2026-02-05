package com.pw.edu.pl.master.thesis.ai.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pw.edu.pl.master.thesis.ai.client.issue.IssueDetailsClient;
import com.pw.edu.pl.master.thesis.ai.client.jira.JiraIssueClient;
import com.pw.edu.pl.master.thesis.ai.dto.ai.AIAnalysisRequest;
import com.pw.edu.pl.master.thesis.ai.dto.ai.AttachmentRequest;
import com.pw.edu.pl.master.thesis.ai.dto.issue.issuedetails.IssueDetails;
import com.pw.edu.pl.master.thesis.ai.service.AIService;
import com.pw.edu.pl.master.thesis.ai.service.DeepSeekService;
import com.pw.edu.pl.master.thesis.ai.service.GeminiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIServiceImplementation implements AIService {

    private final IssueDetailsClient issueDetailsClient;
    private final JiraIssueClient jiraIssueClient;
    private final ObjectMapper objectMapper;

    private final DeepSeekService deepSeekService;
    private final GeminiService geminiService;

    @Override
    public Map<String, Object> analyze(AIAnalysisRequest request) {

        if (request.getIssueKey() == null || request.getIssueKey().isBlank()) {
            throw new IllegalArgumentException("issueKey is required");
        }

        // --------------------------------------------------
        // 1️⃣ Fetch Jira issue
        // --------------------------------------------------
        IssueDetails details =
                issueDetailsClient.getIssueDetails(request.getIssueKey());

        // --------------------------------------------------
        // 2️⃣ Compute ACTUAL resolution time (GROUND TRUTH)
        // --------------------------------------------------
        Double actualResolutionHours =
                calculateActualResolutionHours(details);

        if (actualResolutionHours == null) {
            log.info("Issue {} is unresolved or missing resolution date",
                    request.getIssueKey());
        }

        // ✅ SINGLE SOURCE OF TRUTH
        request.setActualResolutionHours(actualResolutionHours);

        // --------------------------------------------------
        // 3️⃣ Serialize issue JSON
        // --------------------------------------------------
        String issueJson;
        try {
            issueJson = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(details);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize issue JSON", e);
        }

        request.setIssueJson(issueJson);

        // --------------------------------------------------
        // 4️⃣ Optional attachment
        // --------------------------------------------------
        try {
            var resp = jiraIssueClient.downloadFirstAttachment(request.getIssueKey());

            AttachmentRequest attachment = new AttachmentRequest();
            attachment.setFileBytes(resp.getBody());
            attachment.setFilename(
                    resp.getHeaders().getContentDisposition().getFilename()
            );
            attachment.setMimeType(
                    resp.getHeaders().getContentType() != null
                            ? resp.getHeaders().getContentType().toString()
                            : null
            );

            request.setAttachment(attachment);
        } catch (Exception ignored) {
            log.debug("No attachment found for issue {}", request.getIssueKey());
        }

        // --------------------------------------------------
        // 5️⃣ Route to AI models
        // --------------------------------------------------
        Map<String, Object> response = new HashMap<>();
        response.put("issueKey", request.getIssueKey());
        response.put("actualResolutionHours", actualResolutionHours);

        switch (request.getAiModel()) {

            case DEEPSEEK -> response.put(
                    "deepseek",
                    deepSeekService.generateFromIssue(request)
            );

            case GEMINI -> response.put(
                    "gemini",
                    geminiService.generateFromIssue(request)
            );

            case BOTH -> {
                response.put(
                        "deepseek",
                        deepSeekService.generateFromIssue(request)
                );
                response.put(
                        "gemini",
                        geminiService.generateFromIssue(request)
                );
            }

            default -> throw new IllegalArgumentException(
                    "Unsupported AI model: " + request.getAiModel()
            );
        }

        return response;
    }

    // ------------------------------------------------------
    // 🔹 Ground Truth Calculation (Jira lifecycle)
    // ------------------------------------------------------
    private Double calculateActualResolutionHours(IssueDetails issue) {

        if (issue == null ||
                issue.getFields() == null ||
                issue.getFields().getCreated() == null ||
                issue.getFields().getResolutionDate() == null) {
            return null; // unresolved issue
        }

        OffsetDateTime created =
                issue.getFields().getCreated();
        OffsetDateTime resolved =
                issue.getFields().getResolutionDate();

        long seconds =
                Duration.between(created, resolved).getSeconds();

        return seconds / 3600.0;
    }
}

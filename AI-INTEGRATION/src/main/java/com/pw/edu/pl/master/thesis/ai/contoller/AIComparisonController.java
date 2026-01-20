package com.pw.edu.pl.master.thesis.ai.contoller;

import com.pw.edu.pl.master.thesis.ai.dto.ai.AIAnalysisRequest;
import com.pw.edu.pl.master.thesis.ai.service.DeepSeekService;
import com.pw.edu.pl.master.thesis.ai.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/wut/ai/compare")
@RequiredArgsConstructor
public class AIComparisonController {

    private final GeminiService geminiService;
    private final DeepSeekService deepSeekService;

    /**
     * Calls BOTH Gemini and DeepSeek with the SAME request
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> compareModels(@RequestBody AIAnalysisRequest request) {

        String geminiResponse = geminiService.generateFromIssue(request);
        String deepSeekResponse = deepSeekService.generateFromIssue(request);
        return ResponseEntity.ok(
                Map.of(
                        "issueKey", request.getIssueKey(),
                        "responses", Map.of(
                                "gemini", geminiResponse,
                                "deepseek", deepSeekResponse
                        )
                )
        );
    }
}

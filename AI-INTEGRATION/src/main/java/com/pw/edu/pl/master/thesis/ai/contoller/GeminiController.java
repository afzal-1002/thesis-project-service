package com.pw.edu.pl.master.thesis.ai.contoller;

import com.pw.edu.pl.master.thesis.ai.dto.ai.AIAnalysisRequest;
import com.pw.edu.pl.master.thesis.ai.dto.ai.ChatMessage;
import com.pw.edu.pl.master.thesis.ai.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/wut/model/gemini")
@RequiredArgsConstructor
public class GeminiController {

    private final GeminiService geminiService;

    // =====================================================
    // SIMPLE CHAT
    // =====================================================
    @PostMapping("/chat")
    public ResponseEntity<String> chat(
            @RequestBody ChatMessage message
    ) {
        return ResponseEntity.ok(geminiService.chat(message));
    }

    // =====================================================
    // ISSUE-BASED ANALYSIS (DTO ONLY)
    // =====================================================
    @PostMapping
    public ResponseEntity<Map<String, Object>> analyze(
            @RequestBody AIAnalysisRequest request
    ) {
        return ResponseEntity.ok(
                Map.of(
                        "issueKey", request.getIssueKey(),
                        "generation", geminiService.generateFromIssue(request)
                )
        );
    }
}

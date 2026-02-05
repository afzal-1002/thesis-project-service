package com.pw.edu.pl.master.thesis.ai.contoller;

import com.pw.edu.pl.master.thesis.ai.dto.ai.AIAnalysisRequest;
import com.pw.edu.pl.master.thesis.ai.dto.ai.deepseek.DeepSeekChatRequest;
import com.pw.edu.pl.master.thesis.ai.dto.ai.deepseek.DeepSeekChatResponse;
import com.pw.edu.pl.master.thesis.ai.service.DeepSeekService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/api/wut/model/deepseek")
@RequiredArgsConstructor
public class DeepSeekController {

    private final DeepSeekService deepSeekService;

    // =====================================================
    // CHAT
    // =====================================================
    @PostMapping("/chat")
    public ResponseEntity<DeepSeekChatResponse> chat(
            @RequestBody DeepSeekChatRequest request
    ) {
        return ResponseEntity.ok(deepSeekService.chat(request));
    }

    // =====================================================
    // SIMPLE GENERATE
    // =====================================================
    @GetMapping("/generate")
    public ResponseEntity<Map<String, Object>> generate(
            @RequestParam String message
    ) {
        return ResponseEntity.ok(
                Map.of("generation", deepSeekService.generate(message))
        );
    }

    // =====================================================
    // STREAMING GENERATE
    // =====================================================
    @GetMapping(
            value = "/generate/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public Flux<String> generateStream(
            @RequestParam String message
    ) {
        return deepSeekService.generateStream(message);
    }

    // =====================================================
    // ISSUE-BASED ANALYSIS (DTO ONLY)
    // =====================================================
    @PostMapping
    public ResponseEntity<Map<String, Object>> analyze(@RequestBody AIAnalysisRequest request)
    {
        return ResponseEntity.ok(
                Map.of(
                        "issueKey", request.getIssueKey(),
                        "generation", deepSeekService.generateFromIssue(request)
                )
        );
    }
}

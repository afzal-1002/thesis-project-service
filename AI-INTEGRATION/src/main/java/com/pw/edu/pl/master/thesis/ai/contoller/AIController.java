package com.pw.edu.pl.master.thesis.ai.contoller;

import com.pw.edu.pl.master.thesis.ai.dto.ai.AIAnalysisRequest;
import com.pw.edu.pl.master.thesis.ai.service.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/wut/ai/analyze")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> analyze(
            @RequestBody AIAnalysisRequest request
    ) {
        return ResponseEntity.ok(aiService.analyze(request));
    }
}

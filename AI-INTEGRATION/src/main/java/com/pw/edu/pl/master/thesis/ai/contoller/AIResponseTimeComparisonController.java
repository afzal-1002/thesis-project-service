package com.pw.edu.pl.master.thesis.ai.contoller;

import com.pw.edu.pl.master.thesis.ai.dto.ai.AIResponseTimeComparisonResult;
import com.pw.edu.pl.master.thesis.ai.service.AIResponseTimeComparisonService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wut/ai/comparison/response-time")
@RequiredArgsConstructor
public class AIResponseTimeComparisonController {

    private final AIResponseTimeComparisonService service;

    @GetMapping
    public List<AIResponseTimeComparisonResult> compare() {
        return service.compareResponseTimes();
    }

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        return service.responseTimeSummary();
    }
}

package com.pw.edu.pl.master.thesis.ai.contoller;

import com.pw.edu.pl.master.thesis.ai.model.AIModelComparisonResult;
import com.pw.edu.pl.master.thesis.ai.service.AIModelComparisonService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wut/ai/comparison")
@RequiredArgsConstructor
public class AIModelComparisonController {

    private final AIModelComparisonService service;

    @GetMapping("/full")
    public List<AIModelComparisonResult> fullComparison() {
        return service.compareModels();
    }

    @GetMapping("/performance")
    public Map<String, Object> performance() {
        return service.performanceComparison();
    }

    @GetMapping("/estimation")
    public Map<String, Object> estimation() {
        return service.estimationComparison();
    }

    @GetMapping("/content")
    public Map<String, Object> contentQuality() {
        return service.contentQualityComparison();
    }

    @GetMapping("/stability")
    public Map<String, Object> stability() {
        return service.stabilityComparison();
    }

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        return service.finalSummary();
    }
}

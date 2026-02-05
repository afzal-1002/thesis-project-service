package com.pw.edu.pl.master.thesis.ai.contoller;

import com.pw.edu.pl.master.thesis.ai.service.AIAnalysisExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wut/ai/metrics/export")
@RequiredArgsConstructor
public class AIAnalysisExportController {

    private final AIAnalysisExportService exportService;

    @GetMapping("/csv")
    public ResponseEntity<String> exportCsv() {

        String csv = exportService.exportMetricsAsCsv();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=ai_analysis_metrics.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(csv);
    }
}

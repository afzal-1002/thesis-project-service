package com.pw.edu.pl.master.thesis.ai.service.implementation;

import com.pw.edu.pl.master.thesis.ai.model.AIModel.AIAnalysisMetric;
import com.pw.edu.pl.master.thesis.ai.repository.AIAnalysisMetricRepository;
import com.pw.edu.pl.master.thesis.ai.service.AIAnalysisExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AIAnalysisExportServiceImplementation
        implements AIAnalysisExportService {

    private final AIAnalysisMetricRepository repository;

    @Override
    public String exportMetricsAsCsv() {

        List<AIAnalysisMetric> all = repository.findAll();

        StringBuilder csv = new StringBuilder();
        csv.append("issueKey,provider,model,analysisTimeMs,analysisTimeSec,estimatedHours,estimatedDays,markdown,explanation,createdAt\n");

        for (AIAnalysisMetric m : all) {
            csv.append(String.format(
                    "%s,%s,%s,%d,%.3f,%s,%s,%s,%s,%s\n",
                    m.getIssueKey(),
                    m.getAiProvider(),
                    m.getAiModel(),
                    m.getAnalysisTimeMs(),
                    m.getAnalysisTimeSec(),
                    m.getEstimatedResolutionHours(),
                    m.getEstimatedResolutionDays(),
                    m.getMarkdownEnabled(),
                    m.getExplanationEnabled(),
                    m.getCreatedAt()
            ));
        }
        return csv.toString();
    }
}

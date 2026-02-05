package com.pw.edu.pl.master.thesis.ai.dto.ai;

import com.pw.edu.pl.master.thesis.ai.enums.AIModelType;
import lombok.Data;

@Data
public class AIAnalysisRequest {

    private String issueKey;

    private String userPrompt;
    private String issueJson;

    private boolean markdown;
    private boolean explanation;

    private AttachmentRequest attachment;

    private AIModelType aiModel;

    private Double temperature;


    // ✅ ADD THIS (GROUND TRUTH)
    private Double actualResolutionHours;
}

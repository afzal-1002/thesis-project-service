package com.pw.edu.pl.master.thesis.ai.dto.ai;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiEstimationDto {
    private String  issueKey;
    private Double  estimatedTime;
    private Double  confidence;
    private OffsetDateTime createdAt;
    private String  notes;
}

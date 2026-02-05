package com.pw.edu.pl.master.thesis.ai.dto.ai.deepseek;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class DeepSeekChatRequest {
    private String prompt;
    private String model;
    private Double temperature;
    private Integer maxTokens;
}
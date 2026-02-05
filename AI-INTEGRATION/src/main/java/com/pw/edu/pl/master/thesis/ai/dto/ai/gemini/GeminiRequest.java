package com.pw.edu.pl.master.thesis.ai.dto.ai.gemini;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pw.edu.pl.master.thesis.ai.model.AIModel.gemini.Content;
import com.pw.edu.pl.master.thesis.ai.model.AIModel.gemini.GenerationConfig;
import com.pw.edu.pl.master.thesis.ai.model.AIModel.gemini.SafetySetting;
import com.pw.edu.pl.master.thesis.ai.model.AIModel.gemini.SystemInstruction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @NoArgsConstructor
@AllArgsConstructor
public class GeminiRequest {

    @JsonProperty("systemInstruction")
    private SystemInstruction systemInstruction;

    private List<Content> contents;

    @JsonProperty("generationConfig")
    private GenerationConfig generationConfig;

    @JsonProperty("safetySettings")
    private List<SafetySetting> safetySettings;
}
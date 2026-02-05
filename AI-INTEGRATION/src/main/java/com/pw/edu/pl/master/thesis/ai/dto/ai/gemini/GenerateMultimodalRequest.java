package com.pw.edu.pl.master.thesis.ai.dto.ai.gemini;

import com.pw.edu.pl.master.thesis.ai.model.AIModel.gemini.GenerationConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateMultimodalRequest {
    private String promptText;
    private String base64ImageData;
    private String mimeType;
    private GenerationConfig generationConfig;
}


package com.pw.edu.pl.master.thesis.ai.dto.ai;

import com.pw.edu.pl.master.thesis.ai.model.AIModel.gemini.Content;
import com.pw.edu.pl.master.thesis.ai.model.AIModel.gemini.GenerationConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    private List<Content> contents;
    private GenerationConfig generationConfig;
}


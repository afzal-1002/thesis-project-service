package com.pw.edu.pl.master.thesis.ai.dto.ai.gemini;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateTextRequest {
    private String promptText;
}

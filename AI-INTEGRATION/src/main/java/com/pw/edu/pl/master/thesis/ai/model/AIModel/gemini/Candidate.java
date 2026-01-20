package com.pw.edu.pl.master.thesis.ai.model.AIModel.gemini;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @NoArgsConstructor
@AllArgsConstructor
public class Candidate {
    private Content content;
    @JsonProperty("finishReason")
    private String finishReason;
    @JsonProperty("safetyRatings")
    private List<SafetyRating> safetyRatings;
    @JsonProperty("citationMetadata")
    private CitationMetadata citationMetadata;

}


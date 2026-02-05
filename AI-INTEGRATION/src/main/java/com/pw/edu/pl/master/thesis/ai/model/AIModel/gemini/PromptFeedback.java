package com.pw.edu.pl.master.thesis.ai.model.AIModel.gemini;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data @NoArgsConstructor
@AllArgsConstructor
public class PromptFeedback {
    @JsonProperty("safetyRatings")
    private List<SafetyRating> safetyRatings;
}

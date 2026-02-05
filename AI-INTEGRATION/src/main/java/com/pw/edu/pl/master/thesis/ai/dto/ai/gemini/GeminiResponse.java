package com.pw.edu.pl.master.thesis.ai.dto.ai.gemini;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pw.edu.pl.master.thesis.ai.model.AIModel.gemini.Candidate;
import com.pw.edu.pl.master.thesis.ai.model.AIModel.gemini.PromptFeedback;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data @NoArgsConstructor
@AllArgsConstructor @Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeminiResponse {

    // Duration in seconds
    private Long timeTaken;

    // Time when AI call was started
    private OffsetDateTime startTime;

    // Time when AI call finished
    private OffsetDateTime endTime;

    private List<Candidate> candidates;

    @JsonProperty("promptFeedback")
    private PromptFeedback promptFeedback;
}
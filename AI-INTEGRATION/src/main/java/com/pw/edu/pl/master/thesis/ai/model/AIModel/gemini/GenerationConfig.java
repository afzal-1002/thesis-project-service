package com.pw.edu.pl.master.thesis.ai.model.AIModel.gemini;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class GenerationConfig {

    private Double temperature;

    @JsonProperty("topP")
    private Double topP;

    @JsonProperty("topK")
    private Integer topK;

    @JsonProperty("maxOutputTokens")
    private Integer maxOutputTokens;

    @JsonProperty("stopSequences")
    private List<String> stopSequences;

}

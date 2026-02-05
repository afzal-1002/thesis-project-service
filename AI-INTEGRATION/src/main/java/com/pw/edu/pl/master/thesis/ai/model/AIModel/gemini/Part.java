package com.pw.edu.pl.master.thesis.ai.model.AIModel.gemini;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
public  class Part {
    private String text;
    @JsonProperty("inlineData")
    private InlineData inlineData;
    public Part(String text) { this.text = text; }
    public Part(InlineData inlineData) { this.inlineData = inlineData; }
}

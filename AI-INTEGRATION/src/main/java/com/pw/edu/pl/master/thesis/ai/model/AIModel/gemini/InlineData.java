package com.pw.edu.pl.master.thesis.ai.model.AIModel.gemini;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data @NoArgsConstructor
@AllArgsConstructor
public class InlineData {
    @JsonProperty("mimeType")
    private String mimeType;
    private String data;

}
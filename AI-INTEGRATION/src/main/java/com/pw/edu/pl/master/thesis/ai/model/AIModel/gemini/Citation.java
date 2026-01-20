package com.pw.edu.pl.master.thesis.ai.model.AIModel.gemini;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor
@AllArgsConstructor
public class Citation {

    @JsonProperty("startIndex")
    private Integer startIndex;

    @JsonProperty("endIndex")
    private Integer endIndex;

    private String uri;
    private String title;

    @JsonProperty("license")
    private String license;

    @JsonProperty("publicationDate")
    private String publicationDate;
}
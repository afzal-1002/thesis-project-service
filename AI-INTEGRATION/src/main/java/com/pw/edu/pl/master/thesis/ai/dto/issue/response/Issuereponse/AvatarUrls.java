package com.pw.edu.pl.master.thesis.ai.dto.issue.response.Issuereponse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data @Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public  class AvatarUrls {
    @JsonProperty("48x48")
    private String large;
    @JsonProperty("24x24")
    private String medium;
    @JsonProperty("16x16")
    private String small;
    @JsonProperty("32x32")
    private String xlarge;
}

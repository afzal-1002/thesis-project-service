package com.pw.edu.pl.master.thesis.ai.dto.issue.response.Issuereponse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data @Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public  class AggregateProgress {
    private Integer progress;
    private Integer total;
    private Integer percent;
}


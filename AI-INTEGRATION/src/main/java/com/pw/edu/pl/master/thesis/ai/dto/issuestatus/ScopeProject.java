package com.pw.edu.pl.master.thesis.ai.dto.issuestatus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public  class ScopeProject {
    private String id;
    private String key;
    private String name;
    private String self;
}
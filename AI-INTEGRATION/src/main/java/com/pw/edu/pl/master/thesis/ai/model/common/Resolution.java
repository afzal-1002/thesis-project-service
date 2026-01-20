package com.pw.edu.pl.master.thesis.ai.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Resolution {
    private String self;
    private String id;
    private String name;
    private String description;
}


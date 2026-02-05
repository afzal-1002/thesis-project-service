package com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

// src/main/java/.../dto/issue/response/Visibility.java
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Visibility {
    private String type;
    private String value;
    private String identifier;
}

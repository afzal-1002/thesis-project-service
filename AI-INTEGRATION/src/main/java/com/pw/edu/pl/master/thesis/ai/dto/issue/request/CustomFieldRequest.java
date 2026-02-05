package com.pw.edu.pl.master.thesis.ai.dto.issue.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor
@AllArgsConstructor @Builder
public class CustomFieldRequest {
    private String name;
    private String description;
    private String type;
    private String searcherKey;
}

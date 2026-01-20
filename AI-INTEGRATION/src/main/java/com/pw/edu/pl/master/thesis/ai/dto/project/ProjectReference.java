package com.pw.edu.pl.master.thesis.ai.dto.project;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectReference {
    private Long id;
    private String key;

    public static ProjectReference of(Long id, String key) {
        return new ProjectReference(id, key);
    }
}
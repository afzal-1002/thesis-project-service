package com.pw.edu.pl.master.thesis.ai.dto.project;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DeleteProjectRequest {
    private String projectKey;
    private String username;
}

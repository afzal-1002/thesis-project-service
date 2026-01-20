package com.pw.edu.pl.master.thesis.ai.dto.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor
@AllArgsConstructor
public class SyncProjectRequest {
    private String projectKey;
}

package com.pw.edu.pl.master.thesis.issues.dto.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor
@AllArgsConstructor
public class SyncAllProjectRequest {
        private String username;
}

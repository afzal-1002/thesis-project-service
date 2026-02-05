package com.pw.edu.pl.master.thesis.ai.dto.issue.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor @Builder
public class DeleteIssuesRequest {
    List<String> issueKeys;
}

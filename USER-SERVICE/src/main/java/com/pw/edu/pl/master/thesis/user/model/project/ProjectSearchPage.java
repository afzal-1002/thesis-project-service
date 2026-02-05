package com.pw.edu.pl.master.thesis.user.model.project;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectSearchPage {
    private Integer startAt;
    private Integer maxResults;
    private Integer total;
    private List<ProjectSummary> values;
}

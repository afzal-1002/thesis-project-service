package com.pw.edu.pl.master.thesis.ai.dto.project;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Version {
    private String self;
    private String id;
    private String description;
    private String name;
    private boolean archived;
    private boolean released;
    private String releaseDate;
    private String userReleaseDate;
    private String projectId;
    private String startDate;
    private String userStartDate;
}
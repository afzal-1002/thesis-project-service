package com.pw.edu.pl.master.thesis.project.dto.project;

import lombok.Data;

@Data
public class ProjectTypeRequest {
    private String projectTypeKey; // "software" | "business" | "service_desk"
}

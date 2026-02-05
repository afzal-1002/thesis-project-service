package com.pw.edu.pl.master.thesis.project.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StyleOption {
    private String key;          // "scrum", "kanban", "basic", "itsm", ...
    private String label;        // "Scrum", "Kanban", "Basic", "IT Service Management"
    private String templateKey;  // preferred template to use if this style is chosen
}
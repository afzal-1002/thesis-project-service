package com.pw.edu.pl.master.thesis.ai.dto.issue.response;

import lombok.Data;

import java.util.List;

@Data
public class CustomFieldResponse {
    private String id;
    private String name;
    private List<String> clauseNames;
}
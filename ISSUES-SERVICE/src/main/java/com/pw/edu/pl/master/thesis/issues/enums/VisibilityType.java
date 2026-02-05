package com.pw.edu.pl.master.thesis.issues.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VisibilityType {
    ROLE("role"),
    GROUP("group");

    private final String jiraValue;
}

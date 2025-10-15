package com.ii.pw.edu.pl.master.thesis.project.model;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Getter @Builder @Data
public class RequestContext {
    private String projectKey;
}

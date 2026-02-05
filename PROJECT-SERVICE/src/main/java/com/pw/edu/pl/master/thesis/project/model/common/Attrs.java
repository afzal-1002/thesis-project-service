package com.pw.edu.pl.master.thesis.project.model.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Attrs {
    private String text;
    private String color;
    private String localId;
    private String style;
    private String layout;
    private String id;
    private String collection;
    private Integer height;
    private Integer width;
    private String alt;
}
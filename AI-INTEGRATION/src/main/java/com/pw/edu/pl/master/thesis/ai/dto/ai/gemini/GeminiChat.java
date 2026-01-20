package com.pw.edu.pl.master.thesis.ai.dto.ai.gemini;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeminiChat {
    private String id;
    private String author;
    private String body;
    private OffsetDateTime created;

}
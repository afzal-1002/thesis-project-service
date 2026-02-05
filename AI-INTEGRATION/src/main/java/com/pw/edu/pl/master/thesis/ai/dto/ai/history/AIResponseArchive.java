package com.pw.edu.pl.master.thesis.ai.dto.ai.history;

public record AIResponseArchive(
        String aiProvider,
        String userPrompt,
        String content,
        Object createdAt
) {}

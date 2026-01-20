package com.pw.edu.pl.master.thesis.ai.dto.ai.history;

public record AccuracyTrendResult(
         String date,
         Double avgError,
        String timeBucket,
        Double meanAbsoluteError
) {}


package com.pw.edu.pl.master.thesis.ai.service;

import org.springframework.stereotype.Component;

@Component
public interface AIPromptBuilder {

    /**
     * Builds a STRICT JSON-only prompt.
     * This prompt is used ONLY for estimation metrics parsing.
     */
    String buildEstimationJsonPrompt(String issueJson);

    /**
     * Builds a human-readable prompt.
     * Controlled by markdown + explanation flags.
     */
    String buildHumanReadablePrompt(
            String issueJson,
            boolean markdown,
            boolean explanation
    );
}

package com.pw.edu.pl.master.thesis.ai.service.implementation;

import com.pw.edu.pl.master.thesis.ai.service.AIPromptBuilder;
import org.springframework.stereotype.Component;

@Component
public class AIPromptBuilderImplementation implements AIPromptBuilder {


    @Override
    public String buildEstimationJsonPrompt(String issueJson) {

        return """
            You are an expert Jira bug triage and estimation assistant.

            Analyze the following issue and RETURN ONLY VALID JSON
            in the EXACT format below.

            {
              "summary": "string",
              "resolutionSteps": ["step1", "step2"],
              "risks": ["risk1", "risk2"],
              "estimatedResolutionHours": number,
              "estimatedResolutionDays": number
            }

            STRICT RULES:
            - DO NOT include markdown
            - DO NOT include explanations
            - DO NOT include text outside JSON
            - DO NOT wrap JSON in code blocks

            --- Issue JSON ---
            %s
            --- End ---
            """.formatted(issueJson);
    }

    // =====================================================
    // HUMAN-READABLE PROMPT
    // =====================================================
    @Override
    public String buildHumanReadablePrompt(
            String issueJson,
            boolean markdown,
            boolean explanation
    ) {

        StringBuilder prompt = new StringBuilder();

        if (markdown && explanation) {
            prompt.append("Analyze the issue using MARKDOWN with DETAILED EXPLANATIONS.\n\n");
        } else if (markdown) {
            prompt.append("Analyze the issue using MARKDOWN.\n\n");
        } else if (explanation) {
            prompt.append("Analyze the issue and INCLUDE CLEAR EXPLANATIONS.\n\n");
        } else {
            prompt.append("Analyze the issue and provide a concise response.\n\n");
        }

        prompt.append("""
            Provide the following sections:
            1. Summary
            2. Resolution steps
            3. Risks
            4. Estimated resolution time (textual)

            --- Issue JSON ---
            %s
            --- End ---
            """.formatted(issueJson));

        return prompt.toString();
    }
}

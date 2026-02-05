package com.pw.edu.pl.master.thesis.ai.dto.ai.deepseek;

import lombok.*;

import java.util.List;

@Data @Builder
@NoArgsConstructor @AllArgsConstructor
@Getter @Setter
public class DeepSeekChatResponse {
    private String id;
    private String object;
    private long created;
    private String model;
    private List<Choice> choices;



    @Data @Builder
    public static class Choice {
        private int index;
        private DeepSeekMessage message;
        private String finish_reason;
    }

    @Data
    public static class Usage {
        private int prompt_tokens;
        private int completion_tokens;
        private int total_tokens;
    }

    private Usage usage;
}
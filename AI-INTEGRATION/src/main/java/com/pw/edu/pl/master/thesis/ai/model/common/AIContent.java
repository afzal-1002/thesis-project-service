package com.pw.edu.pl.master.thesis.ai.model.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AIContent {

    private String type;
    private int version;
    private List<ContentItem> contentItems;

    @Data
    @NoArgsConstructor @AllArgsConstructor @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ContentItem {

        private String type;
        private String text;
        private InlineData inlineData;
        private List<Part> parts;

        @Data
        @NoArgsConstructor @AllArgsConstructor @Builder
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class Part {

            private String text;
            private InlineData inlineData;

            public Part(String text) {
                this.text = text;
                this.inlineData = null;
            }

            public Part(InlineData inlineData) {
                this.inlineData = inlineData;
                this.text = null;
            }
        }
    }

    @Data
    @NoArgsConstructor @AllArgsConstructor @Builder
    public static class InlineData {
        private String type;
        private String data;
    }
}

package com.pw.edu.pl.master.thesis.project.model.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Body {
    private String type;
    private int version;
    private List<Content> content;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Content {
        private String type;
        private List<ContentItem> contentItem;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentItem {
        private String type;
        private String text;
    }


    public static List<String> extractTextFromBody(Body body) {
        List<String> comments = new ArrayList<>();

        for (Content content : body.getContent()) {
            if ("paragraph".equals(content.getType())) {
                StringBuilder comment = new StringBuilder();
                for (ContentItem contentItem : content.getContentItem()) {
                    if ("text".equals(contentItem.getType())) {
                        comment.append(contentItem.getText()).append(" ");
                    }
                }
                if (!comment.isEmpty()) { comments.add("Comment: " + comment.toString().trim()); }
            }
        }
        return comments;
    }


}
package com.pw.edu.pl.master.thesis.ai.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class IssueLink {
    private String       id;
    private LinkType     type;
    private OutwardIssue outwardIssue;
    private InwardIssue  inwardIssue;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LinkType {
        private String id;
        private String inward;
        private String name;
        private String outward;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OutwardIssue {
        private String     id;
        private String     key;
        private String     self;
        private BasicFields fields;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class BasicFields {
            private ContentStatus status;
            private String        summary;

            @Data
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class ContentStatus {
                private String iconUrl;
                private String name;
            }
        }
    }


    @Data
    @EqualsAndHashCode(callSuper = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InwardIssue extends OutwardIssue { }

}

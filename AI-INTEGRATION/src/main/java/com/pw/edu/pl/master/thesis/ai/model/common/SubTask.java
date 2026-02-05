package com.pw.edu.pl.master.thesis.ai.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubTask {
    private String id;
    private String key;
    private String self;
    private BasicFields fields;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BasicFields {
        private Status status;
        private String summary;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Status {
            private String iconUrl;
            private String name;
        }
    }
}

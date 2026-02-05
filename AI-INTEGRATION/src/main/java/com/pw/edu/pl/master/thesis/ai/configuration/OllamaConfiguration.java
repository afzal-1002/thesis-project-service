//package com.pw.edu.pl.master.thesis.ai.configuration;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.ai.ollama.api.OllamaApi;
//import org.springframework.ai.ollama.OllamaChatModel;
//import org.springframework.ai.ollama.api.OllamaOptions;
//
//@Configuration
//public class OllamaConfiguration {
//
//    @Bean
//    public OllamaApi ollamaApi() {
//        return OllamaApi.builder()
//                .baseUrl("http://localhost:11434")
//                .build();
//    }
//
//    @Bean
//    public OllamaChatModel ollamaChatModel(OllamaApi api) {
//        return OllamaChatModel.builder()
//                .ollamaApi(api)
//                .defaultOptions(
//                        OllamaOptions.builder()
//                                .model("deepseek-r1:8b")
//                                .temperature(0.9)
//                                .build())
//                .build();
//    }
//}

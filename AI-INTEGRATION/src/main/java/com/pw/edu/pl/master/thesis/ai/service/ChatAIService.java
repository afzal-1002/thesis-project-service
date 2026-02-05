//// Service: use the bean
//package com.pw.edu.pl.master.thesis.ai.service;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.ai.chat.model.ChatResponse;
//import org.springframework.ai.chat.prompt.Prompt;
//import org.springframework.ai.ollama.OllamaChatModel;
//import org.springframework.stereotype.Service;
//import reactor.core.publisher.Flux;
//
//@Service
//@RequiredArgsConstructor
//public class ChatAIService {
//
//    private final OllamaChatModel chatModel;
//    private final ChatClient chatClient;
//
//
//    public ChatResponse generatePirates() {
//        return chatModel.call(new Prompt("Generate the names of 5 famous pirates."));
//    }
//
//    public Flux<ChatResponse> streamPirates() {
//        return chatModel.stream(new Prompt("Generate the names of 5 famous pirates."));
//    }
//
//    public Flux<String> streamResponse(String question) {
//        return this.chatClient.prompt().user(question).stream().content();
//    }
//}

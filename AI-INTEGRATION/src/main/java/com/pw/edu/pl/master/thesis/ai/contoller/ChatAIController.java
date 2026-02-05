package com.pw.edu.pl.master.thesis.ai.contoller;

import com.pw.edu.pl.master.thesis.ai.dto.ai.ChatMessage;
//import com.pw.edu.pl.master.thesis.ai.service.ChatAIService;
import com.pw.edu.pl.master.thesis.ai.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/wut/ai/playground")
@RequiredArgsConstructor
public class ChatAIController {

    private final GeminiService geminiService;

    @PostMapping("/chat")
    public String chat(@RequestBody String text) {
        ChatMessage message = ChatMessage.builder()
                .role("user")
                .content(text)
                .build();

        return geminiService.chat(message);
    }

}

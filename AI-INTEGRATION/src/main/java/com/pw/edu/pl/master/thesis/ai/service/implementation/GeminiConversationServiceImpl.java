package com.pw.edu.pl.master.thesis.ai.service.implementation;


import com.pw.edu.pl.master.thesis.ai.dto.ai.gemini.GeminiChat;
import com.pw.edu.pl.master.thesis.ai.service.GeminiConversationService;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GeminiConversationServiceImpl implements GeminiConversationService {

    private final Map<String, List<GeminiChat>> sessions = new ConcurrentHashMap<>();

    @Override
    public String startSession(String systemPrompt) {
        String sessionId = UUID.randomUUID().toString();
        GeminiChat systemTurn =
                new GeminiChat(null, "system", systemPrompt, OffsetDateTime.now());
        sessions.put(sessionId, new ArrayList<>(List.of(systemTurn)));
        return sessionId;
    }

    @Override
    public void append(String sessionId, GeminiChat chat) {
        sessions.computeIfAbsent(sessionId, id -> new ArrayList<>())
                .add(chat);
    }

    @Override
    public List<GeminiChat> getHistory(String sessionId) {
        return Collections.unmodifiableList(
                sessions.getOrDefault(sessionId, Collections.emptyList())
        );
    }

    @Override
    public void clearSession(String sessionId) {
        sessions.remove(sessionId);
    }
}

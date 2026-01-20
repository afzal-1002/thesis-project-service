package com.pw.edu.pl.master.thesis.ai.service;


import com.pw.edu.pl.master.thesis.ai.dto.ai.gemini.GeminiChat;

import java.util.List;

public interface GeminiConversationService {

    String startSession(String systemPrompt);
    void append(String sessionId, GeminiChat chat);
    List<GeminiChat> getHistory(String sessionId);
    void clearSession(String sessionId);
}

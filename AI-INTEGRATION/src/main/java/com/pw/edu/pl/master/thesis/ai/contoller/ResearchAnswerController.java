package com.pw.edu.pl.master.thesis.ai.contoller;

import com.pw.edu.pl.master.thesis.ai.model.ResearchAnswer;
import com.pw.edu.pl.master.thesis.ai.service.ResearchAnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wut/ai/research-questions")
@RequiredArgsConstructor
public class ResearchAnswerController {

    private final ResearchAnswerService service;

    /**
     * UI endpoint – display questions & answers
     */
    @GetMapping("/qa")
    public List<Map<String, Object>> getQuestionsAndAnswers() {
        return service.getAllQuestionsAndAnswers();
    }

    /**
     * Admin endpoint – add or update answer
     */
    @PostMapping("/{questionId}/answer")
    public ResearchAnswer updateAnswer(
            @PathVariable Long questionId,
            @RequestBody Map<String, String> body
    ) {
        return service.saveOrUpdateAnswer(
                questionId,
                body.get("answer")
        );
    }

    /**
     * Search questions by text (question content).
     * Example: /qa/search?text=AI
     */
    @GetMapping("/qa/search")
    public List<Map<String, Object>> searchQuestionsAndAnswers(
            @RequestParam String text
    ) {
        return service.searchQuestionsAndAnswers(text);
    }

}

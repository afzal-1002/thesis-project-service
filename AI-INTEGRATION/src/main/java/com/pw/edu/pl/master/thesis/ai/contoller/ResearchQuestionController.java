package com.pw.edu.pl.master.thesis.ai.contoller;

import com.pw.edu.pl.master.thesis.ai.model.ResearchQuestion;
import com.pw.edu.pl.master.thesis.ai.service.ResearchQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wut/ai/research-questions")
@RequiredArgsConstructor
public class ResearchQuestionController {

    private final ResearchQuestionService service;

    @GetMapping
    public List<ResearchQuestion> getActiveQuestions() {
        return service.getActiveResearchQuestions();
    }

    @PostMapping
    public ResearchQuestion save(@RequestBody ResearchQuestion question) {
        return service.save(question);
    }
}

package com.pw.edu.pl.master.thesis.ai.service.implementation;

import com.pw.edu.pl.master.thesis.ai.model.ResearchQuestion;
import com.pw.edu.pl.master.thesis.ai.repository.ResearchQuestionRepository;
import com.pw.edu.pl.master.thesis.ai.service.ResearchQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResearchQuestionServiceImplementation
        implements ResearchQuestionService {

    private final ResearchQuestionRepository repository;

    @Override
    public List<ResearchQuestion> getActiveResearchQuestions() {
        return repository.findByActiveTrue();
    }

    @Override
    public ResearchQuestion save(ResearchQuestion question) {
        return repository.save(question);
    }
}

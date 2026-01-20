package com.pw.edu.pl.master.thesis.ai.service;



import com.pw.edu.pl.master.thesis.ai.model.ResearchQuestion;

import java.util.List;

public interface ResearchQuestionService {

    List<ResearchQuestion> getActiveResearchQuestions();
    ResearchQuestion save(ResearchQuestion question);
}

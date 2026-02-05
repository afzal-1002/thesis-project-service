package com.pw.edu.pl.master.thesis.ai.service;

import com.pw.edu.pl.master.thesis.ai.model.ResearchAnswer;
import com.pw.edu.pl.master.thesis.ai.model.ResearchQuestion;

import java.util.List;
import java.util.Map;

public interface ResearchAnswerService {

    List<Map<String, Object>> getAllQuestionsAndAnswers();
    ResearchAnswer saveOrUpdateAnswer(Long researchQuestionId, String answer);
    List<Map<String, Object>> searchQuestionsAndAnswers(String text);
}

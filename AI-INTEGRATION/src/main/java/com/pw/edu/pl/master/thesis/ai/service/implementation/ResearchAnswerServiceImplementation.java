package com.pw.edu.pl.master.thesis.ai.service.implementation;


import com.pw.edu.pl.master.thesis.ai.model.ResearchQuestion;
import com.pw.edu.pl.master.thesis.ai.model.ResearchAnswer;
import com.pw.edu.pl.master.thesis.ai.repository.ResearchAnswerRepository;
import com.pw.edu.pl.master.thesis.ai.repository.ResearchQuestionRepository;
import com.pw.edu.pl.master.thesis.ai.service.ResearchAnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ResearchAnswerServiceImplementation
        implements ResearchAnswerService {

    private final ResearchQuestionRepository questionRepository;
    private final ResearchAnswerRepository answerRepository;

    @Override
    public List<Map<String, Object>> getAllQuestionsAndAnswers() {

        List<ResearchQuestion> questions = questionRepository.findByActiveTrue();

        return questions.stream().map(q -> {

            ResearchAnswer answer = answerRepository
                            .findByResearchQuestionId(q.getId())
                            .orElse(null);

            Map<String, Object> result = new HashMap<>();
            result.put("questionId", q.getId());
            result.put("code", q.getCode());
            result.put("question", q.getQuestion());
            result.put("answer", answer != null
                    ? answer.getAnswer()
                    : "Not available");

            return result;

        }).toList();
    }


    @Override
    public ResearchAnswer saveOrUpdateAnswer(
            Long researchQuestionId,
            String answerText
    ) {

        ResearchQuestion question =
                questionRepository.findById(researchQuestionId)
                        .orElseThrow(() ->
                                new RuntimeException("Question not found"));

        ResearchAnswer answer =
                answerRepository
                        .findByResearchQuestionId(researchQuestionId)
                        .orElse(
                                ResearchAnswer.builder()
                                        .researchQuestion(question)
                                        .build()
                        );

        answer.setAnswer(answerText);
        return answerRepository.save(answer);
    }

    @Override
    public List<Map<String, Object>> searchQuestionsAndAnswers(String text) {

        List<ResearchQuestion> questions =
                questionRepository
                        .findByActiveTrueAndQuestionContainingIgnoreCase(text);

        return questions.stream().map(q -> {

            ResearchAnswer answer =
                    answerRepository
                            .findByResearchQuestionId(q.getId())
                            .orElse(null);

            Map<String, Object> result = new HashMap<>();
            result.put("questionId", q.getId());
            result.put("code", q.getCode());
            result.put("question", q.getQuestion());
            result.put("answer", answer != null
                    ? answer.getAnswer()
                    : "Not available");

            return result;

        }).toList();
    }


}

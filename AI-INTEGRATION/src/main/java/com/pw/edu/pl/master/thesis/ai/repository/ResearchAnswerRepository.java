package com.pw.edu.pl.master.thesis.ai.repository;

import com.pw.edu.pl.master.thesis.ai.model.ResearchAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResearchAnswerRepository
        extends JpaRepository<ResearchAnswer, Long> {

    Optional<ResearchAnswer>  findByResearchQuestionId(Long researchQuestionId);
}

package com.pw.edu.pl.master.thesis.ai.repository;


import com.pw.edu.pl.master.thesis.ai.model.ResearchQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResearchQuestionRepository extends JpaRepository<ResearchQuestion, Long> {

    List<ResearchQuestion> findByActiveTrue();
    List<ResearchQuestion>  findByActiveTrueAndQuestionContainingIgnoreCase(String text);
}

package com.pw.edu.pl.master.thesis.ai.repository;

import com.pw.edu.pl.master.thesis.ai.model.AIModel.AIModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AIModelRepository extends JpaRepository<AIModel, Long> {
}

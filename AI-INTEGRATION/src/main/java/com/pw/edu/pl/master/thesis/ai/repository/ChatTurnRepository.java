package com.pw.edu.pl.master.thesis.ai.repository;

import com.pw.edu.pl.master.thesis.ai.model.AIModel.ChatTurn;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface ChatTurnRepository extends JpaRepository<ChatTurn, Long> {
    List<ChatTurn> findBySessionIdOrderByIdAsc(String sessionId);
}

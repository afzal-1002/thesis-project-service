package com.pw.edu.pl.master.thesis.ai.model.AIModel;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "chat_turns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatTurn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "role")
    private String role;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    private OffsetDateTime createdAt;

    @PrePersist
    void pre() {
        createdAt = OffsetDateTime.now();
    }
}

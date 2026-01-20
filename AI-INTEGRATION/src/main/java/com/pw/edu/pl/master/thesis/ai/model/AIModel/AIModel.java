package com.pw.edu.pl.master.thesis.ai.model.AIModel;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_models")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "version", nullable = false)
    private String version;

    @Column(name = "provider", nullable = false)
    private String provider;

    @Column(name = "trained_date")
    private LocalDateTime trainedDate;

    @Column(name = "metrics", columnDefinition = "json")
    private String metrics;
}

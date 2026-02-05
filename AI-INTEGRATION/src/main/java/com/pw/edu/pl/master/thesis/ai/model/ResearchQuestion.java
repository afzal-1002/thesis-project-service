package com.pw.edu.pl.master.thesis.ai.model;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "research_question")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResearchQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false, length = 1000)
    private String question;

    @Column(nullable = false)
    private Boolean active;
}

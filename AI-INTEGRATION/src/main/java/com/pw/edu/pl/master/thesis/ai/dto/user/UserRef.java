package com.pw.edu.pl.master.thesis.ai.dto.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "users_ref")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRef {
    @Id
    @Column(length = 64)
    private String id;
}
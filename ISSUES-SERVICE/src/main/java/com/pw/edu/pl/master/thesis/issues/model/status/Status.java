package com.pw.edu.pl.master.thesis.issues.model.status;

import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.StatusCategory;
import com.pw.edu.pl.master.thesis.issues.dto.issuestatus.Scope;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table
@Getter @Setter @NoArgsConstructor
@AllArgsConstructor @Builder @Data
public class Status {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String self;

    @Column(name = "jira_id", unique = true)
    private String jiraId;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride( name = "id", column = @Column(name = "category_jira_id")),
            @AttributeOverride(name = "name", column = @Column(name = "category_name")),
            @AttributeOverride(name = "self", column = @Column(name = "category_self_url", length = 500)),
            @AttributeOverride(name = "key", column = @Column(name = "category_key_value")),
            @AttributeOverride(name = "colorName", column = @Column(name = "color_name"))
    })
    private StatusCategory category;

    @Transient
    private Scope scope;
}

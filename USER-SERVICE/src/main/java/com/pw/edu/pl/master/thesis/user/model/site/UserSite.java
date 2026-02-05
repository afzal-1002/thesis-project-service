package com.pw.edu.pl.master.thesis.user.model.site;


import com.pw.edu.pl.master.thesis.user.model.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "user_site")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class UserSite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_site_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_site_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "site_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_site_site"))
    private Site site;

    @Column(name = "is_default_for_user")
    private Boolean defaultForUser;

    @CreationTimestamp
    @Column(name = "linked_at", nullable = false, updatable = false)
    private OffsetDateTime linkedAt;
}

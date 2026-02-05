package com.pw.edu.pl.master.thesis.user.model.site;

import com.pw.edu.pl.master.thesis.user.model.user.UserCredential;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "site")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "site_id")
    private Long id;

    @Column(name = "site_name", nullable = false, length = 120)
    private String siteName;

    @Column(name = "host_part", length = 200)
    private String hostPart;

    @Column(name = "base_url", nullable = false, length = 400)
    private String baseUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime creationDate;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updateDate;

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<UserSite> userLinks = new HashSet<>();

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<SiteProject> projects = new HashSet<>();


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credential_id", foreignKey = @ForeignKey(name = "fk_site_user_credential"))
    private UserCredential credential;   // <-- name MUST match mappedBy


    @PrePersist @PreUpdate
    void normalize() {
        if (siteName != null) siteName = siteName.trim();
        if (hostPart != null) hostPart = hostPart.trim();
        if (baseUrl  != null) baseUrl  = SiteURLUtility.normalizeURL(baseUrl);
    }
}

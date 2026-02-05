package com.pw.edu.pl.master.thesis.user.model.user;


import com.pw.edu.pl.master.thesis.user.enums.Role;
import com.pw.edu.pl.master.thesis.user.model.project.UserProject;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name="first_name", nullable = false)
    private String firstName;

    @Column(name="last_name", nullable = false)
    private String lastName;

    @Column(name="account_id", nullable = false)
    private String accountId;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "email_address", nullable = false, unique = true)
    private String emailAddress;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @ElementCollection
    @CollectionTable(name="user_roles", joinColumns=@JoinColumn(name="user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name="role", length=32, nullable=false)
    private Set<Role> roles = new HashSet<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserCredential userCredential;


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserProject> projects = new HashSet<>();

    // helpers
    public void addProject(String projectKey, String role) {
        UserProject up = new UserProject(this, projectKey, role);
        projects.add(up);
    }
    public void removeProject(String projectKey) {
        projects.removeIf(up -> up.getProjectKey().equals(projectKey));
    }

}

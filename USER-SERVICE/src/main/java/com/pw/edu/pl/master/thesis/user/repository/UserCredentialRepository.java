package com.pw.edu.pl.master.thesis.user.repository;

import com.pw.edu.pl.master.thesis.user.model.user.AppUser;
import com.pw.edu.pl.master.thesis.user.model.user.UserCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCredentialRepository extends JpaRepository<UserCredential, Long> {
    Optional<UserCredential> findByUserId(Long userId);
    Optional<UserCredential> findByUsername(String jiraUsername);
    Optional<UserCredential> findByUser_Id(Long userId);
    boolean existsByUsername(String jiraUsername);
    boolean existsByAccountId(String accountId);
    Optional<UserCredential> findByBaseUrlIgnoreCase(String jiraBaseUrl);
    Optional<UserCredential> findByAccountId(String accountId);

    Optional<UserCredential> findByUser(AppUser user);


}
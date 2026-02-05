package com.pw.edu.pl.master.thesis.user.repository;

import com.pw.edu.pl.master.thesis.user.model.user.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailAddress(String emailAddress);

    @EntityGraph(attributePaths = "projects")
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmailAddress(String emailAddress);

    Optional<User> findByUsernameAndPassword(String username, String password);
    Optional<User> findByEmailAddressAndPassword(String emailAddress, String password);

    long deleteByUsername(String username);
    long deleteByEmailAddress(String emailAddress);

    Optional<User> findByAccountId(String accountId);
    Optional<User> findByEmailAddressIgnoreCase(String email);




}

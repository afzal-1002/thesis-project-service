package com.pw.edu.pl.master.thesis.user.service;

import com.pw.edu.pl.master.thesis.user.dto.user.LoginRequest;
import com.pw.edu.pl.master.thesis.user.dto.user.RegisterUserRequest;
import com.pw.edu.pl.master.thesis.user.dto.user.UserRequest;
import com.pw.edu.pl.master.thesis.user.model.user.User;
import com.pw.edu.pl.master.thesis.user.dto.user.UserSummary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public interface UserService {
    // Registration / auth (stateless)
    UserSummary registerUser(RegisterUserRequest request);
    UserSummary userLogin(LoginRequest request); // <- no HttpSession

    String verifyAccountId(String leadAccountId, String jiraBaseUrl, String jiraPlainToken, String username);

    UserSummary getUserById(Long id);

    @Transactional(readOnly = true)
    User findUserById(Long userId);

    List<UserSummary> getAllUsers();
    Optional<User> findByJiraAccountId(String accountId);
    @Transactional(readOnly = true)
    UserSummary findByAccountId(String accountId);


    @Transactional(readOnly = true)
    UserSummary findByUsername(String userName);

    // Update
    UserSummary updateUser(Long id, UserRequest request);
    UserSummary updateUser(User userInput);

    // Save / delete
    User saveAndFlushUser(User user);
    public String deleteUserAndCredentials(String username);

    // Roles / existence checks
    List<String> getAllRoleNames();
    List<String> getRoleNamesByUsername(String username);
    boolean existsByEmailAddress(String emailAddress);
    boolean existsByUsername(String username);
    @Transactional(readOnly = true)
    UserSummary findByEmailAddress(String email);

}

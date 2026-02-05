package com.pw.edu.pl.master.thesis.user.service.implementation;

import com.pw.edu.pl.master.thesis.user.configuration.AuthContext;
import com.pw.edu.pl.master.thesis.user.configuration.JiraClientConfiguration;
import com.pw.edu.pl.master.thesis.user.dto.credentials.UserCredentialRequest;
import com.pw.edu.pl.master.thesis.user.dto.site.AddSiteRequest;
import com.pw.edu.pl.master.thesis.user.dto.user.JiraUserMeResponse;
import com.pw.edu.pl.master.thesis.user.dto.user.JiraUserResponse;
import com.pw.edu.pl.master.thesis.user.dto.user.LoginRequest;
import com.pw.edu.pl.master.thesis.user.dto.user.RegisterUserRequest;
import com.pw.edu.pl.master.thesis.user.dto.user.UserRequest;
import com.pw.edu.pl.master.thesis.user.dto.user.UserSummary;
import com.pw.edu.pl.master.thesis.user.enums.JiraApiEndpoint;
import com.pw.edu.pl.master.thesis.user.enums.Role;
import com.pw.edu.pl.master.thesis.user.exception.InvalidCredentialsException;
import com.pw.edu.pl.master.thesis.user.exception.ResourceNotFoundException;
import com.pw.edu.pl.master.thesis.user.exception.UserAlreadyExistException;
import com.pw.edu.pl.master.thesis.user.exception.UserNotFoundException;
import com.pw.edu.pl.master.thesis.user.exception.jiraexception.InvalidJiraCredentialException;
import com.pw.edu.pl.master.thesis.user.exception.jiraexception.JiraAuthenticationException;
import com.pw.edu.pl.master.thesis.user.mapper.UserMapper;
import com.pw.edu.pl.master.thesis.user.model.helper.HelperMethod;
import com.pw.edu.pl.master.thesis.user.model.helper.JiraUrlBuilder;
import com.pw.edu.pl.master.thesis.user.model.user.AppUser;
import com.pw.edu.pl.master.thesis.user.model.user.User;
import com.pw.edu.pl.master.thesis.user.model.user.UserCredential;
import com.pw.edu.pl.master.thesis.user.repository.SiteRepository;
import com.pw.edu.pl.master.thesis.user.repository.UserRepository;
import com.pw.edu.pl.master.thesis.user.service.*;
import com.pw.edu.pl.master.thesis.user.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImplementation implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CredentialService credentialService;
    private final EncryptionService encryptionService;
    private final @Lazy JiraUserService jiraUserService;
    private final HelperMethod helperMethod;
    private final SiteRepository siteRepository;
    private final SiteService siteService;
    private final JiraClientConfiguration jiraClientConfiguration;
    private final JiraUrlBuilder jiraUrlBuilder;
    private final AppUserService appUserService;
    private final AuthContext auth;

    /* ---------------------------------------------------------------------
     * REGISTER (public)
     * ------------------------------------------------------------------- */
    @Override
    @Transactional

    public UserSummary registerUser(RegisterUserRequest request) {
        final String baseUrl  = jiraUrlBuilder.normalizeJiraBaseUrl(request.getBaserUrl());
        final String siteName = helperMethod.getSiteName(baseUrl);

        log.info("Base URL: {} ",  baseUrl);
        log.info("Site Name: {} " , siteName);

        // 1) Create Spring-Security user (Basic auth)
        AppUser appUser =  appUserService.register(request.getUsername(), request.getPassword(), "USER");

        // 2) Uniqueness checks for domain User
        if (existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistException("Username already taken");
        }
        if (existsByEmailAddress(request.getEmailAddress())) {
            throw new UserAlreadyExistException("Email already registered");
        }

        // 3) Validate Jira token & read profile (use PLAIN token here)
        final JiraUserResponse profile;
        try {
            profile = jiraUserService.getJiraUserDetailsRaw(baseUrl, request.getUsername(), request.getJiraToken());
            log.info("Jira user profile found: {}", profile);
        } catch (JiraAuthenticationException ex) {
            throw new InvalidJiraCredentialException("Jira token invalid or expired");
        }

        if (credentialService.existsByAccountId(profile.getAccountId())) {
            throw new UserAlreadyExistException("This Jira account is already registered");
        }

        // 4) Build roles
        Set<Role> roles = (request.getRoles() == null || request.getRoles().isEmpty())
                ? Set.of(Role.USER)
                : request.getRoles().stream()
                .filter(Objects::nonNull)
                .map(this::parseRole)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // 5) Create domain User (hash password)
        User user = User.builder()
                .username(request.getUsername())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .password(encryptionService.hashPassword(request.getPassword()))
                .emailAddress(request.getEmailAddress())
                .phoneNumber(request.getPhoneNumber())
                .accountId(profile.getAccountId())
                .isActive(true)
                .roles(roles)
                .build();
        try {
            user = userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            throw new UserAlreadyExistException("Registration failed: " + ex.getMostSpecificCause().getMessage());
        }



        UserCredentialRequest credentialRequest = UserCredentialRequest.builder()
                .username(request.getUsername())
                .accountId(profile.getAccountId())
                .token(request.getJiraToken()) // plain; service encrypts
                .baseUrl(baseUrl)
                .createdAt(OffsetDateTime.now())
                .build();

      UserCredential userCredential =  credentialService.addCredentialAndLinkToUsers(credentialRequest, user, appUser);

      log.info("User credential added: {}", userCredential);

        // 7) Create Site (once per baseUrl)
        siteRepository.findByBaseUrl(baseUrl).ifPresent(existing -> {
            throw new UserAlreadyExistException("A site for this Jira base URL already exists: " + baseUrl);
        });




        AddSiteRequest addSiteRequest = AddSiteRequest.builder()
                                        .siteName(siteName.toUpperCase())
                                        .baseUrl(baseUrl)
                                        .username(userCredential.getUsername())
                                        .jiraToken(request.getJiraToken())
                                        .hostPart(siteName)
                                        .build();

        log.info("Adding site: {}", addSiteRequest.toString());


        siteService.addNewSite(addSiteRequest);

        // 8) Response
        return UserSummary.builder()
                .id(user.getId())
                .username(request.getUsername())
                .baseUrl(baseUrl)
                .accountId(profile.getAccountId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .emailAddress(user.getEmailAddress())
                .phoneNumber(user.getPhoneNumber())
                .displayName(profile.getDisplayName())
                .active(user.isActive())
                .roles(new ArrayList<>(roles))
                .build();
    }

    /* ---------------------------------------------------------------------
     * LOGIN (public)
     * ------------------------------------------------------------------- */
    @Override
    @Transactional(readOnly = true)
    public UserSummary userLogin(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        if (!encryptionService.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        UserCredential cred = credentialService.getUserCredential(request.getUsername());

        String displayName = user.getUsername();
        try {
            String baseUrl = cred.getBaseUrl();
            String tokenPlain = encryptionService.decrypt(cred.getToken()).getResult();
            String meUrl = jiraUrlBuilder.url(baseUrl, JiraApiEndpoint.ME);
            JiraUserResponse profile = jiraClientConfiguration
                    .get(meUrl, JiraUserResponse.class, request.getUsername(), tokenPlain);
            if (profile != null && profile.getDisplayName() != null && !profile.getDisplayName().isBlank()) {
                displayName = profile.getDisplayName();
            }
        } catch (Exception ignore) {
            // keep login successful even if Jira "me" fails
        }

        return UserSummary.builder()
                .id(user.getId())
                .username(cred.getUsername())
                .baseUrl(cred.getBaseUrl())
                .accountId(cred.getAccountId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .emailAddress(user.getEmailAddress())
                .phoneNumber(user.getPhoneNumber())
                .displayName(displayName)
                .active(user.isActive())
                .roles(new ArrayList<>(user.getRoles()))
                .build();
    }

    /* ---------------------------------------------------------------------
     * Uses Basic-auth identity instead of username param
     * ------------------------------------------------------------------- */
    @Override
    public String verifyAccountId(String leadAccountId, String jiraBaseUrl, String jiraPlainToken, String ignoredUsernameParam) {
        String currentUsername = auth.currentUsernameOrThrow();
        if (leadAccountId != null && !leadAccountId.isBlank()) return leadAccountId;

        String normalizedBaseUrl = jiraUrlBuilder.normalizeJiraBaseUrl(jiraBaseUrl);

        // Fetch stored token for the authenticated user
        var cred = credentialService.getUserCredential(currentUsername);
        String tokenPlain = encryptionService.decrypt(cred.getToken()).getResult();

        String meUrl = jiraUrlBuilder.url(normalizedBaseUrl, JiraApiEndpoint.ME);
        JiraUserMeResponse meResponse = jiraClientConfiguration.get(
                meUrl, JiraUserMeResponse.class, currentUsername, tokenPlain);

        if (meResponse == null || meResponse.getAccountId() == null || meResponse.getAccountId().isBlank()) {
            throw new IllegalStateException("Unable to resolve leadAccountId from Jira /myself.");
        }
        return meResponse.getAccountId();
    }

    @Override
    public UserSummary findByUsername(String ignoredParam) {
        String currentUsername = auth.currentUsernameOrThrow();
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + currentUsername));
        return userMapper.toUserSummary(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getRoleNamesByUsername(String ignoredParam) {
        String currentUsername = auth.currentUsernameOrThrow();
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUsername));
        Set<Role> roles = user.getRoles();
        if (roles == null) return List.of();
        return roles.stream().map(Enum::name).toList();
    }

    @Override
    @Transactional
    public String deleteUserAndCredentials(String ignoredParam) {
        String currentUsername = auth.currentUsernameOrThrow();

        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUsername));

        boolean hadCredentials = credentialService.existsByJiraUsername(currentUsername);
        if (hadCredentials) {
            credentialService.deleteCredential(currentUsername);
        }

        userRepository.delete(user);

        return hadCredentials ? "User and credentials deleted" : "User deleted";
    }

    /* ---------------------------------------------------------------------
     * Basic lookups / CRUD (unchanged behavior, no username params)
     * ------------------------------------------------------------------- */
    @Override
    @Transactional(readOnly = true)
    public UserSummary getUserById(Long id) {
        return userMapper.toUserSummary(findUserById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummary> getAllUsers() {
        return userRepository.findAll().stream().map(userMapper::toUserSummary).toList();
    }

    @Override
    public Optional<User> findByJiraAccountId(String accountId) {
        return Optional.empty();
    }

    @Override
    @Transactional
    public UserSummary updateUser(Long id, UserRequest request) {
        User existing = findUserById(id);

        if (request.getFirstName() != null && !Objects.equals(request.getFirstName(), existing.getFirstName()))
            existing.setFirstName(request.getFirstName());

        if (request.getLastName() != null && !Objects.equals(request.getLastName(), existing.getLastName()))
            existing.setLastName(request.getLastName());

        if (request.getUserName() != null && !Objects.equals(request.getUserName(), existing.getUsername())) {
            userRepository.findByUsername(request.getUserName()).ifPresent(u -> {
                throw new UserAlreadyExistException("Username already in use: " + request.getUserName());
            });
            existing.setUsername(request.getUserName());
        }

        if (request.getEmailAddress() != null && !Objects.equals(request.getEmailAddress(), existing.getEmailAddress())) {
            userRepository.findByEmailAddress(request.getEmailAddress()).ifPresent(u -> {
                throw new UserAlreadyExistException("Email already in use: " + request.getEmailAddress());
            });
            existing.setEmailAddress(request.getEmailAddress());
        }

        if (request.getIsActive() != null) existing.setActive(request.getIsActive());
        if (request.getPassword() != null) existing.setPassword(encryptionService.hashPassword(request.getPassword()));

        try {
            existing = userRepository.saveAndFlush(existing);
        } catch (DataIntegrityViolationException ex) {
            throw new UserAlreadyExistException("Update failed: " + ex.getMostSpecificCause().getMessage());
        }
        return userMapper.toUserSummary(existing);
    }

    @Override
    @Transactional
    public UserSummary updateUser(User userInput) {
        if (userInput.getId() == null) throw new IllegalArgumentException("User ID required");

        User existing = findUserById(userInput.getId());
        if (userInput.getUsername() != null)     existing.setUsername(userInput.getUsername());
        if (userInput.getEmailAddress() != null) existing.setEmailAddress(userInput.getEmailAddress());
        if (userInput.getFirstName() != null)    existing.setFirstName(userInput.getFirstName());
        if (userInput.getLastName() != null)     existing.setLastName(userInput.getLastName());
        if (userInput.getPhoneNumber() != null)  existing.setPhoneNumber(userInput.getPhoneNumber());
        existing.setActive(userInput.isActive());
        if (userInput.getPassword() != null)     existing.setPassword(encryptionService.hashPassword(userInput.getPassword()));

        return userMapper.toUserSummary(userRepository.saveAndFlush(existing));
    }

    @Override
    @Transactional(readOnly = true)
    public UserSummary findByAccountId(String accountId) {
        User user = userRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with accountId: " + accountId));
        return userMapper.toUserSummary(user);
    }

    @Override
    @Transactional
    public User saveAndFlushUser(User user) {
        return userRepository.saveAndFlush(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllRoleNames() {
        return Arrays.stream(Role.values()).map(Enum::name).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmailAddress(String emailAddress) {
        if (emailAddress == null || emailAddress.isBlank()) {
            throw new IllegalArgumentException("Email address cannot be null or empty");
        }
        return userRepository.existsByEmailAddress(emailAddress);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public UserSummary findByEmailAddress(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }

        return userRepository.findByEmailAddress(email)
                .map(userMapper::toUserSummary)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    /* ----------------------- helpers ----------------------- */
    private Role parseRole(String role) {
        String userRole = role.trim().replace('-', '_').replace(' ', '_').toUpperCase(Locale.ROOT);
        try { return Role.valueOf(userRole); }
        catch (IllegalArgumentException e) { throw new IllegalArgumentException("Unknown role: " + role); }
    }
}

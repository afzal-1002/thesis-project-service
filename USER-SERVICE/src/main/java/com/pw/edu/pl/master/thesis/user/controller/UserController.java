package com.pw.edu.pl.master.thesis.user.controller;

import com.pw.edu.pl.master.thesis.user.dto.user.LoginRequest;
import com.pw.edu.pl.master.thesis.user.dto.user.RegisterUserRequest;
import com.pw.edu.pl.master.thesis.user.dto.user.UserRequest;
import com.pw.edu.pl.master.thesis.user.dto.user.UserSummary;
import com.pw.edu.pl.master.thesis.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wut/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    // ── Auth (stateless) ───────────────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<UserSummary> registerUser(@RequestBody RegisterUserRequest request) {
        UserSummary resp = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @PostMapping("/login")
    public ResponseEntity<UserSummary> userLogin(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.userLogin(request));
    }

//    @PostMapping("/login")
//    public ResponseEntity<UserSummary> userLogin(@RequestBody LoginRequest request,
//                                                 HttpServletRequest httpRequest) {
//        UserSummary user = userService.userLogin(request);
//        if (user == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//        }
//
//        // create HTTP session so server will set JSESSIONID cookie
//        HttpSession session = httpRequest.getSession(true);
//        session.setAttribute("CURRENT_USER", user);
//
//        return ResponseEntity.ok(user);
//    }

    @PostMapping("/logout")
    public ResponseEntity<Void> userLogout() {
        return ResponseEntity.noContent().build();
    }

    // ── Reads ──────────────────────────────────────────────────────────
    @GetMapping("/by-email")
    public ResponseEntity<UserSummary> getByEmail(@RequestParam("email") String email) {
        log.info("GET /api/wut/users/by-email?email={}", email);
        UserSummary user = userService.findByEmailAddress(email);
        return (user == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserSummary> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping
    public ResponseEntity<List<UserSummary>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /** Current user profile via Basic principal (no username param). */
    @GetMapping("/me")
    public ResponseEntity<UserSummary> me() {
        return ResponseEntity.ok(userService.findByUsername(null));
    }

    // ── Update ─────────────────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<UserSummary> updateUser(@PathVariable Long id, @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    // ── Delete ─────────────────────────────────────────────────────────
    /** Delete CURRENT user (no username param). */
    @DeleteMapping("/me")
    public ResponseEntity<Map<String, String>> deleteCurrentUser() {
        String message = userService.deleteUserAndCredentials(null);
        return ResponseEntity.ok(Map.of("message", message));
    }

    // ── Roles ──────────────────────────────────────────────────────────
    @GetMapping("/roles")
    public ResponseEntity<List<String>> getAllRoles() {
        return ResponseEntity.ok(userService.getAllRoleNames());
    }

    /** Roles for CURRENT user (no username param). */
    @GetMapping("/me/roles")
    public ResponseEntity<List<String>> getMyRoles() {
        return ResponseEntity.ok(userService.getRoleNamesByUsername(null));
    }

    // (Admin/utility endpoint kept, if you still need to look up arbitrary users)
    @GetMapping("/username/{username}")
    public ResponseEntity<UserSummary> findByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.findByUsername(username));
    }
}

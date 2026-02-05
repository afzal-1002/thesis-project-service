package com.pw.edu.pl.master.thesis.project.client;


import com.pw.edu.pl.master.thesis.project.configuration.FeignSecurityConfiguration;
import com.pw.edu.pl.master.thesis.project.dto.user.LoginRequest;
import com.pw.edu.pl.master.thesis.project.dto.user.RegisterUserRequest;
import com.pw.edu.pl.master.thesis.project.dto.user.UserRequest;
import com.pw.edu.pl.master.thesis.project.dto.user.UserSummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@FeignClient( name = "user-service",
//        url = "${user.service.url}",
//        path = "/api/wut/users",
//        configuration = FeignSecurityConfiguration.class
//)

@FeignClient(
        name = "USER-SERVICE",
        contextId = "UserClient",
        path = "/api/wut/users",
        configuration = FeignSecurityConfiguration.class
)
public interface UserClient {

    @PostMapping("/register")
    UserSummary registerUser(@RequestBody RegisterUserRequest request);

    @PostMapping("/login")
    UserSummary userLogin(@RequestBody LoginRequest request);

    @PostMapping("/logout")
    void userLogout();

    @GetMapping("/by-email")
    UserSummary getByEmail(@RequestParam("email") String email);

    @GetMapping("/{id}")
    UserSummary getUserById(@PathVariable("id") Long id);

    @GetMapping
    List<UserSummary> getAllUsers();

    @PutMapping("/{id}")
    UserSummary updateUser(@PathVariable("id") Long id, @RequestBody UserRequest request);

    @DeleteMapping("/{username}")
    void deleteUser(@PathVariable("username") String username);

    @GetMapping("/roles")
    List<String> getAllRoles();

    @GetMapping("/{username}/roles")
    List<String> getRoles(@PathVariable("username") String username);

    @GetMapping("/username/{username}")
    UserSummary findByUsername(@PathVariable("username") String username);
}

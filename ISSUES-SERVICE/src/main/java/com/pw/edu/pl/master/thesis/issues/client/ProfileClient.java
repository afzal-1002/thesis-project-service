package com.pw.edu.pl.master.thesis.issues.client;


import com.pw.edu.pl.master.thesis.issues.configuration.FeignSecurityConfiguration;
import com.pw.edu.pl.master.thesis.issues.dto.appuser.AuthUserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

//@FeignClient(name = "profile",
//        contextId = "profile-service",
//        path = "/api/wut/profile",
//        url = "${user.service.url}",
//        configuration = FeignSecurityConfiguration.class
//)

@FeignClient(
        name = "USER-SERVICE",
        contextId = "ProfileClient",
        path = "/api/wut/profile",
        configuration = FeignSecurityConfiguration.class
)
public interface ProfileClient {

    @GetMapping("/me")
    AuthUserDTO getCurrentUserProfile();

    @GetMapping("/by-username/{username}")
    AuthUserDTO getByUsername(@PathVariable("username") String username);

}

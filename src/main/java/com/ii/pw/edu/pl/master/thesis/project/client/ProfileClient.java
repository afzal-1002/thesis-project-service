package com.ii.pw.edu.pl.master.thesis.project.client;

import com.ii.pw.edu.pl.master.thesis.project.configuration.FeignSecurityConfiguration;
import com.ii.pw.edu.pl.master.thesis.project.dto.appuser.AppUserDto;
import com.ii.pw.edu.pl.master.thesis.project.dto.appuser.AuthUserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "profile",
        contextId = "profile-service",
        path = "/api/wut/profile",
        url = "${user.service.url}",
        configuration = FeignSecurityConfiguration.class
)
public interface ProfileClient {

    @GetMapping("/me")
    AuthUserDTO getCurrentUserProfile();

}

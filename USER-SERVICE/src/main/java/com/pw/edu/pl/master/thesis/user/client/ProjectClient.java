package com.pw.edu.pl.master.thesis.user.client;

import com.pw.edu.pl.master.thesis.user.configuration.FeignSecurityConfiguration;
import com.pw.edu.pl.master.thesis.user.dto.project.CreateProjectMinimalRequest;
import com.pw.edu.pl.master.thesis.user.dto.project.JiraProjectResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

//@FeignClient(value= "project", url= "${project_service}")

@FeignClient(
        name = "USER-SERVICE",
        contextId = "ProfileClient",
        path = "/api/wut/profile",
        configuration = FeignSecurityConfiguration.class
)
public interface ProjectClient {

    @RequestMapping(method = RequestMethod.GET, value = "/api/wut/projects")
    JiraProjectResponse createProjectJira(@RequestBody CreateProjectMinimalRequest request);
}

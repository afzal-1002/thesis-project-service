package com.pw.edu.pl.master.thesis.project.service;


import com.pw.edu.pl.master.thesis.project.model.ProjectUser;

import java.util.List;

public interface ProjectUserService {

    /** List members of a project (project key or Jira id), scoped to the caller's baseUrl. */
    List<ProjectUser> listMembers(String projectKeyOrId);

    /** Add (link) a user to a project. displayName is optional; falls back to userId. */
    boolean addMember(String projectKeyOrId, String userId, String displayName);

    /** Remove (unlink) a user from a project. */
    boolean removeMember(String projectKeyOrId, String userId);

    /** Count members of a project. */
    long countMembers(String projectKeyOrId);

    /** Check if a user is already linked to a project. */
    boolean exists(String projectKeyOrId, String userId);
}

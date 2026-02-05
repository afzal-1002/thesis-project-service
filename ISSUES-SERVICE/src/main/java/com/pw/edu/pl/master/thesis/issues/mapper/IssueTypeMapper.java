package com.pw.edu.pl.master.thesis.issues.mapper;

import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.IssueTypeResponse;
import com.pw.edu.pl.master.thesis.issues.dto.issuetype.CreateIssueTypeResponse;
import com.pw.edu.pl.master.thesis.issues.dto.issuetype.IssueTypeSummary;
import com.pw.edu.pl.master.thesis.issues.model.issuetype.IssueType;
import org.springframework.stereotype.Component;

@Component
public class IssueTypeMapper {

    /* -------------------- DTO -> ENTITY -------------------- */

    public IssueType mapFromJira(IssueTypeResponse src) {
        return toEntity(src);
    }

    public IssueType toEntity(IssueTypeSummary src) {
        if (src == null) return null;

        IssueType.IssueTypeBuilder b = IssueType.builder()
                .id(Long.valueOf(src.getId()))
                .self(src.getSelf())
                .name(src.getName())
                .description(src.getDescription())
                .iconUrl(src.getIconUrl())
                .subtask(Boolean.TRUE.equals(src.getSubtask()))
                .avatarId(src.getAvatarId())
                .hierarchyLevel(src.getHierarchyLevel());

        return b.build();
    }
    public IssueType toEntity(IssueTypeResponse src) {
        if (src == null) return null;

        IssueType.IssueTypeBuilder b = IssueType.builder()
                .id(Long.valueOf(src.getId()))
                .self(src.getSelf())
                .name(src.getName())
                .description(src.getDescription())
                .iconUrl(src.getIconUrl())
                .subtask(Boolean.TRUE.equals(src.getSubtask())) // Use isSubtask() if that's the getter
                .avatarId(src.getAvatarId())
                .hierarchyLevel(src.getHierarchyLevel());

        return b.build();
    }


    public IssueType toEntity(CreateIssueTypeResponse src) {
        if (src == null) return null;

        IssueType.IssueTypeBuilder b = IssueType.builder()
                .id(Long.valueOf(src.getId()))
                .self(src.getSelf())
                .name(src.getName())
                .description(src.getDescription())
                .iconUrl(src.getIconUrl())
                .subtask(Boolean.TRUE.equals(src.getSubtask()))
                .avatarId(src.getAvatarId())
                .hierarchyLevel(src.getHierarchyLevel());

        return b.build();
    }

    public void copyInto(IssueTypeSummary src, IssueType target) {
        if (src == null || target == null) return;

        target.setId(src.getId());
        target.setSelf(src.getSelf());
        target.setName(src.getName());
        target.setDescription(src.getDescription());
        target.setIconUrl(src.getIconUrl());
        target.setSubtask(Boolean.TRUE.equals(src.getSubtask()));
        target.setAvatarId(src.getAvatarId());
        target.setHierarchyLevel(src.getHierarchyLevel());
    }

    public void copyInto(CreateIssueTypeResponse src, IssueType target) {
        if (src == null || target == null) return;

        target.setId(Long.valueOf(src.getId()));
        target.setSelf(src.getSelf());
        target.setName(src.getName());
        target.setDescription(src.getDescription());
        target.setIconUrl(src.getIconUrl());
        target.setSubtask(Boolean.TRUE.equals(src.getSubtask()));
        target.setAvatarId(src.getAvatarId());
        target.setHierarchyLevel(src.getHierarchyLevel());
    }

    /* -------------------- ENTITY -> DTO -------------------- */

    public IssueTypeSummary toSummary(IssueType entity) {
        if (entity == null) return null;

        IssueTypeSummary dto = new IssueTypeSummary();
        dto.setId(entity.getId());
        dto.setSelf(entity.getSelf());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setIconUrl(entity.getIconUrl());
        dto.setSubtask(entity.getSubtask());
        dto.setAvatarId(entity.getAvatarId());
        dto.setHierarchyLevel(entity.getHierarchyLevel());
        return dto;
    }
}
package com.pw.edu.pl.master.thesis.issues.dto.issue.issuedetails;

import lombok.*;
import java.util.List;

@Data
@Builder @Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentDetail {
    private String title;
    private boolean hasAttachment;
    private List<CommentMediaRef> mediaRefs;
}
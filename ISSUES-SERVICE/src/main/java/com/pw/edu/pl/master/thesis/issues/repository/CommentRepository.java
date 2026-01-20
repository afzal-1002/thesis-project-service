package com.pw.edu.pl.master.thesis.issues.repository;

import com.pw.edu.pl.master.thesis.issues.enums.SynchronizationStatus;
import com.pw.edu.pl.master.thesis.issues.model.comment.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Sync statusResponse
    List<Comment> findBySynchronizationStatus(SynchronizationStatus syncStatus);
    List<Comment> findByIssueKeyAndSynchronizationStatus(String issueKey, SynchronizationStatus syncStatus);

    // Issue lookups (both relation and denormalized key, if you keep both)
    List<Comment> findByIssue_Key(String key);
    List<Comment> findAllByIssueKey(String issueKey);
    void deleteAllByIssueKey(String issueKey);

    // ---- Jira comment id (String), NOT the PK ----
    Optional<Comment> findByCommentId(String commentId);

    Optional<Comment> findByCommentIdAndSynchronizationStatus(String commentId, SynchronizationStatus synchronizationStatus);
    List<Comment> findAllByCommentIdIn(Collection<String> commentIds);

    // Author
    List<Comment> findAllByAuthorId(String authorId); // if authorId is an accountId (String)
    // If authorId is Long in your entity, change to: List<Comment> findAllByAuthorId(Long authorId);

    List<Comment> findAllByIssue_Key(String issueKey);

    @Query("select c from Comment c where c.aiGenerated = true")
    List<Comment> findAllByAiTrue();

    @Query("""
           select c
           from Comment c
           where c.commentId is null
              or c.pushStatus in (
                   PushStatus.DRAFT,
                   PushStatus.PENDING,
                   PushStatus.FAILED
              )
           order by c.createdAt asc
           """)
    List<Comment> findAllPendingForPush();

}

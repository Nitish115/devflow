package com.devflow.devflow.event;

import java.io.Serializable;
import java.time.LocalDateTime;

public class CommentMentionedEvent implements Serializable {

    private Long commentId;
    private Long taskId;
    private Long projectId;
    private Long organizationId;
    private Long authorId;
    private String authorEmail;
    private Long mentionedUserId;
    private String content;
    private LocalDateTime timestamp;

    public CommentMentionedEvent() {
    }

    public CommentMentionedEvent(Long commentId, Long taskId, Long projectId, Long organizationId,
                                Long authorId, String authorEmail, Long mentionedUserId, String content) {
        this.commentId = commentId;
        this.taskId = taskId;
        this.projectId = projectId;
        this.organizationId = organizationId;
        this.authorId = authorId;
        this.authorEmail = authorEmail;
        this.mentionedUserId = mentionedUserId;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }

    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public Long getMentionedUserId() {
        return mentionedUserId;
    }

    public void setMentionedUserId(Long mentionedUserId) {
        this.mentionedUserId = mentionedUserId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}

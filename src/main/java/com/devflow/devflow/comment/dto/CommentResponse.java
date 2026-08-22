package com.devflow.devflow.comment.dto;

import java.time.LocalDateTime;
import java.util.List;

public class CommentResponse {

    private Long id;
    private Long taskId;
    private Long authorId;
    private String authorEmail;
    private String content;
    private List<Long> mentionedUserIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CommentResponse() {
    }

    public CommentResponse(Long id, Long taskId, Long authorId, String authorEmail,
                           String content, List<Long> mentionedUserIds,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.taskId = taskId;
        this.authorId = authorId;
        this.authorEmail = authorEmail;
        this.content = content;
        this.mentionedUserIds = mentionedUserIds;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<Long> getMentionedUserIds() {
        return mentionedUserIds;
    }

    public void setMentionedUserIds(List<Long> mentionedUserIds) {
        this.mentionedUserIds = mentionedUserIds;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

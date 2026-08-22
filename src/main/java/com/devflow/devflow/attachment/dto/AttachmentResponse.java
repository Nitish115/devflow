package com.devflow.devflow.attachment.dto;

import java.time.LocalDateTime;

public class AttachmentResponse {

    private Long id;
    private Long taskId;
    private Long uploadedById;
    private String uploadedByEmail;
    private String fileName;
    private Long fileSize;
    private String contentType;
    private LocalDateTime createdAt;

    public AttachmentResponse() {
    }

    public AttachmentResponse(Long id, Long taskId, Long uploadedById, String uploadedByEmail,
                              String fileName, Long fileSize, String contentType, LocalDateTime createdAt) {
        this.id = id;
        this.taskId = taskId;
        this.uploadedById = uploadedById;
        this.uploadedByEmail = uploadedByEmail;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.createdAt = createdAt;
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

    public Long getUploadedById() {
        return uploadedById;
    }

    public void setUploadedById(Long uploadedById) {
        this.uploadedById = uploadedById;
    }

    public String getUploadedByEmail() {
        return uploadedByEmail;
    }

    public void setUploadedByEmail(String uploadedByEmail) {
        this.uploadedByEmail = uploadedByEmail;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

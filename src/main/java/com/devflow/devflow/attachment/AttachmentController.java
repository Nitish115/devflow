package com.devflow.devflow.attachment;

import com.devflow.devflow.attachment.dto.AttachmentResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/organizations/{orgId}/projects/{projectId}/tasks/{taskId}/attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachmentResponse> uploadAttachment(@PathVariable Long orgId,
                                                               @PathVariable Long projectId,
                                                               @PathVariable Long taskId,
                                                               @RequestParam("file") MultipartFile file,
                                                               Principal principal) {
        AttachmentResponse response = attachmentService.uploadAttachment(orgId, projectId, taskId, file, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<AttachmentResponse>> getAttachments(@PathVariable Long orgId,
                                                                   @PathVariable Long projectId,
                                                                   @PathVariable Long taskId,
                                                                   Principal principal) {
        List<AttachmentResponse> response = attachmentService.getAttachments(orgId, projectId, taskId, principal.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{attachmentId}/download")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long orgId,
                                                        @PathVariable Long projectId,
                                                        @PathVariable Long taskId,
                                                        @PathVariable Long attachmentId,
                                                        Principal principal) {
        AttachmentService.AttachmentDownload download = attachmentService.getAttachmentDownload(orgId, projectId, taskId, attachmentId, principal.getName());
        Attachment attachment = download.getAttachment();
        Resource resource = download.getResource();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getFileName() + "\"")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(attachment.getFileSize()))
                .body(resource);
    }

    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long orgId,
                                                 @PathVariable Long projectId,
                                                 @PathVariable Long taskId,
                                                 @PathVariable Long attachmentId,
                                                 Principal principal) {
        attachmentService.deleteAttachment(orgId, projectId, taskId, attachmentId, principal.getName());
        return ResponseEntity.noContent().build();
    }
}

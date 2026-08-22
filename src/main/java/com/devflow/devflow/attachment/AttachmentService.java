package com.devflow.devflow.attachment;

import com.devflow.devflow.attachment.dto.AttachmentResponse;
import com.devflow.devflow.attachment.storage.StorageService;
import com.devflow.devflow.auth.Role;
import com.devflow.devflow.auth.User;
import com.devflow.devflow.auth.UserRepository;
import com.devflow.devflow.common.exception.*;
import com.devflow.devflow.organization.OrganizationMember;
import com.devflow.devflow.organization.OrganizationMemberRepository;
import com.devflow.devflow.project.ProjectMember;
import com.devflow.devflow.project.ProjectMemberRepository;
import com.devflow.devflow.project.ProjectService;
import com.devflow.devflow.task.Task;
import com.devflow.devflow.task.TaskRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AttachmentService {

    private static final Set<String> BLOCKED_EXTENSIONS = Set.of(
            "exe", "bat", "cmd", "sh", "php", "jsp", "asp", "aspx",
            "dll", "scr", "com", "vbs", "ps1", "jar", "py", "rb", "cgi"
    );

    private static final Set<String> BLOCKED_CONTENT_TYPES = Set.of(
            "application/x-msdownload",
            "application/x-executable",
            "application/x-sh",
            "application/x-httpd-php",
            "application/x-bat",
            "text/x-php",
            "text/x-python",
            "application/java-archive"
    );

    private final AttachmentRepository attachmentRepository;
    private final StorageService storageService;
    private final TaskRepository taskRepository;
    private final ProjectService projectService;
    private final ProjectMemberRepository projectMemberRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final UserRepository userRepository;
    private final long maxFileSize;

    public AttachmentService(AttachmentRepository attachmentRepository,
                             StorageService storageService,
                             TaskRepository taskRepository,
                             ProjectService projectService,
                             ProjectMemberRepository projectMemberRepository,
                             OrganizationMemberRepository organizationMemberRepository,
                             UserRepository userRepository,
                             @Value("${app.storage.max-file-size:10485760}") long maxFileSize) {
        this.attachmentRepository = attachmentRepository;
        this.storageService = storageService;
        this.taskRepository = taskRepository;
        this.projectService = projectService;
        this.projectMemberRepository = projectMemberRepository;
        this.organizationMemberRepository = organizationMemberRepository;
        this.userRepository = userRepository;
        this.maxFileSize = maxFileSize;
    }

    @Transactional
    public AttachmentResponse uploadAttachment(Long orgId, Long projectId, Long taskId, MultipartFile file, String userEmail) {
        User currentUser = getUserByEmail(userEmail);
        projectService.validateOrgMembership(orgId, currentUser.getId());
        projectService.getProjectEntity(orgId, projectId);
        projectService.validateProjectMembership(projectId, currentUser.getId());

        Task task = getTaskEntity(projectId, taskId);

        validateFile(file);

        String storageKey = storageService.store(file);

        String rawFilename = file.getOriginalFilename();
        String cleanFilename = rawFilename != null ? StringUtils.cleanPath(rawFilename) : "file";
        cleanFilename = java.nio.file.Paths.get(cleanFilename).getFileName().toString();

        String contentType = file.getContentType() != null && !file.getContentType().isBlank()
                ? file.getContentType()
                : "application/octet-stream";

        Attachment attachment = new Attachment(
                task,
                currentUser,
                cleanFilename,
                file.getSize(),
                contentType,
                storageKey
        );

        Attachment savedAttachment = attachmentRepository.save(attachment);
        return mapToAttachmentResponse(savedAttachment);
    }

    @Transactional(readOnly = true)
    public List<AttachmentResponse> getAttachments(Long orgId, Long projectId, Long taskId, String userEmail) {
        User currentUser = getUserByEmail(userEmail);
        projectService.validateOrgMembership(orgId, currentUser.getId());
        projectService.getProjectEntity(orgId, projectId);
        projectService.validateProjectMembership(projectId, currentUser.getId());

        getTaskEntity(projectId, taskId);

        List<Attachment> attachments = attachmentRepository.findAllByTaskIdOrderByCreatedAtAsc(taskId);
        return attachments.stream()
                .map(this::mapToAttachmentResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AttachmentDownload getAttachmentDownload(Long orgId, Long projectId, Long taskId, Long attachmentId, String userEmail) {
        User currentUser = getUserByEmail(userEmail);
        projectService.validateOrgMembership(orgId, currentUser.getId());
        projectService.getProjectEntity(orgId, projectId);
        projectService.validateProjectMembership(projectId, currentUser.getId());

        getTaskEntity(projectId, taskId);

        Attachment attachment = attachmentRepository.findByIdAndTaskId(attachmentId, taskId)
                .orElseThrow(() -> new AttachmentNotFoundException("Attachment not found with id: " + attachmentId + " on task: " + taskId));

        Resource resource = storageService.loadAsResource(attachment.getStorageKey());
        return new AttachmentDownload(attachment, resource);
    }

    @Transactional
    public void deleteAttachment(Long orgId, Long projectId, Long taskId, Long attachmentId, String userEmail) {
        User currentUser = getUserByEmail(userEmail);
        projectService.validateOrgMembership(orgId, currentUser.getId());
        projectService.getProjectEntity(orgId, projectId);
        projectService.validateProjectMembership(projectId, currentUser.getId());

        getTaskEntity(projectId, taskId);

        Attachment attachment = attachmentRepository.findByIdAndTaskId(attachmentId, taskId)
                .orElseThrow(() -> new AttachmentNotFoundException("Attachment not found with id: " + attachmentId + " on task: " + taskId));

        boolean isUploader = attachment.getUploadedBy().getId().equals(currentUser.getId());
        boolean isElevated = canManageProjectOrOrg(orgId, projectId, currentUser.getId());

        if (!isUploader && !isElevated) {
            throw new InsufficientPermissionsException("Only the file uploader, Project Manager, or Organization Owner/Admin can delete this attachment");
        }

        attachmentRepository.delete(attachment);
        storageService.delete(attachment.getStorageKey());
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidOperationException("File must not be empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new FileTooLargeException("File size (" + file.getSize() + " bytes) exceeds maximum allowed limit of " + maxFileSize + " bytes");
        }

        String rawFilename = file.getOriginalFilename();
        if (rawFilename != null) {
            String extension = getFileExtension(rawFilename).toLowerCase();
            if (BLOCKED_EXTENSIONS.contains(extension)) {
                throw new UnsupportedFileTypeException("Files with extension '." + extension + "' are blocked for security reasons");
            }
        }

        String contentType = file.getContentType();
        if (contentType != null && BLOCKED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new UnsupportedFileTypeException("Content type '" + contentType + "' is blocked for security reasons");
        }
    }

    private String getFileExtension(String filename) {
        int lastIndex = filename.lastIndexOf('.');
        if (lastIndex == -1 || lastIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastIndex + 1);
    }

    private Task getTaskEntity(Long projectId, Long taskId) {
        return taskRepository.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId + " in project: " + projectId));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    private boolean canManageProjectOrOrg(Long orgId, Long projectId, Long userId) {
        Optional<OrganizationMember> orgMember = organizationMemberRepository.findByOrganizationIdAndUserId(orgId, userId);
        if (orgMember.isPresent() && (orgMember.get().getRole() == Role.ORGANIZATION_OWNER || orgMember.get().getRole() == Role.ORGANIZATION_ADMIN)) {
            return true;
        }

        Optional<ProjectMember> projectMember = projectMemberRepository.findByProjectIdAndUserId(projectId, userId);
        return projectMember.isPresent() && projectMember.get().getRole() == Role.PROJECT_MANAGER;
    }

    private AttachmentResponse mapToAttachmentResponse(Attachment attachment) {
        return new AttachmentResponse(
                attachment.getId(),
                attachment.getTask().getId(),
                attachment.getUploadedBy().getId(),
                attachment.getUploadedBy().getEmail(),
                attachment.getFileName(),
                attachment.getFileSize(),
                attachment.getContentType(),
                attachment.getCreatedAt()
        );
    }

    public static class AttachmentDownload {
        private final Attachment attachment;
        private final Resource resource;

        public AttachmentDownload(Attachment attachment, Resource resource) {
            this.attachment = attachment;
            this.resource = resource;
        }

        public Attachment getAttachment() {
            return attachment;
        }

        public Resource getResource() {
            return resource;
        }
    }
}

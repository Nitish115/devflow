package com.devflow.devflow.attachment;

import com.devflow.devflow.attachment.dto.AttachmentResponse;
import com.devflow.devflow.attachment.storage.StorageService;
import com.devflow.devflow.auth.Role;
import com.devflow.devflow.auth.User;
import com.devflow.devflow.auth.UserRepository;
import com.devflow.devflow.common.exception.FileTooLargeException;
import com.devflow.devflow.common.exception.InsufficientPermissionsException;
import com.devflow.devflow.common.exception.UnsupportedFileTypeException;
import com.devflow.devflow.organization.Organization;
import com.devflow.devflow.organization.OrganizationMemberRepository;
import com.devflow.devflow.project.*;
import com.devflow.devflow.task.Task;
import com.devflow.devflow.task.TaskPriority;
import com.devflow.devflow.task.TaskRepository;
import com.devflow.devflow.task.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private StorageService storageService;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private OrganizationMemberRepository organizationMemberRepository;

    @Mock
    private UserRepository userRepository;

    private AttachmentService attachmentService;

    private User ownerUser;
    private User devUser;
    private Organization organization;
    private Project project;
    private Task task;
    private Attachment attachment;

    @BeforeEach
    void setUp() {
        attachmentService = new AttachmentService(
                attachmentRepository,
                storageService,
                taskRepository,
                projectService,
                projectMemberRepository,
                organizationMemberRepository,
                userRepository,
                10485760L // 10MB
        );

        ownerUser = new User(1L, "owner@example.com", "password", Role.ORGANIZATION_OWNER, null);
        devUser = new User(2L, "dev@example.com", "password", Role.DEVELOPER, null);

        organization = new Organization(10L, "Acme Corp", "Tech", ownerUser);
        project = new Project(100L, "DevFlow Portal", "PM App", organization, ProjectStatus.ACTIVE, ownerUser);
        task = new Task(500L, project, "Setup DB", "Flyway setup", TaskStatus.TODO, TaskPriority.HIGH, devUser, ownerUser, LocalDate.now().plusDays(7));
        attachment = new Attachment(2000L, task, devUser, "architecture.png", 1024L, "image/png", "uuid_architecture.png");
    }

    @Test
    void testUploadAttachment_Success() {
        MockMultipartFile file = new MockMultipartFile("file", "architecture.png", "image/png", "hello bytes".getBytes());

        when(userRepository.findByEmail("dev@example.com")).thenReturn(Optional.of(devUser));
        when(projectService.getProjectEntity(10L, 100L)).thenReturn(project);
        when(taskRepository.findByIdAndProjectId(500L, 100L)).thenReturn(Optional.of(task));
        when(storageService.store(file)).thenReturn("uuid_architecture.png");
        when(attachmentRepository.save(any(Attachment.class))).thenReturn(attachment);

        AttachmentResponse response = attachmentService.uploadAttachment(10L, 100L, 500L, file, "dev@example.com");

        assertNotNull(response);
        assertEquals(2000L, response.getId());
        assertEquals("architecture.png", response.getFileName());
        verify(storageService).store(file);
        verify(attachmentRepository).save(any(Attachment.class));
    }

    @Test
    void testUploadAttachment_FileTooLarge_ThrowsException() {
        // Construct AttachmentService with max 100 bytes limit for test
        AttachmentService strictAttachmentService = new AttachmentService(
                attachmentRepository, storageService, taskRepository, projectService,
                projectMemberRepository, organizationMemberRepository, userRepository, 100L
        );

        byte[] largeContent = new byte[200];
        MockMultipartFile file = new MockMultipartFile("file", "large.pdf", "application/pdf", largeContent);

        when(userRepository.findByEmail("dev@example.com")).thenReturn(Optional.of(devUser));
        when(projectService.getProjectEntity(10L, 100L)).thenReturn(project);
        when(taskRepository.findByIdAndProjectId(500L, 100L)).thenReturn(Optional.of(task));

        assertThrows(FileTooLargeException.class,
                () -> strictAttachmentService.uploadAttachment(10L, 100L, 500L, file, "dev@example.com"));
    }

    @Test
    void testUploadAttachment_BlockedExtension_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile("file", "script.sh", "text/x-sh", "echo hello".getBytes());

        when(userRepository.findByEmail("dev@example.com")).thenReturn(Optional.of(devUser));
        when(projectService.getProjectEntity(10L, 100L)).thenReturn(project);
        when(taskRepository.findByIdAndProjectId(500L, 100L)).thenReturn(Optional.of(task));

        assertThrows(UnsupportedFileTypeException.class,
                () -> attachmentService.uploadAttachment(10L, 100L, 500L, file, "dev@example.com"));
    }

    @Test
    void testGetAttachments_Success() {
        when(userRepository.findByEmail("dev@example.com")).thenReturn(Optional.of(devUser));
        when(projectService.getProjectEntity(10L, 100L)).thenReturn(project);
        when(taskRepository.findByIdAndProjectId(500L, 100L)).thenReturn(Optional.of(task));
        when(attachmentRepository.findAllByTaskIdOrderByCreatedAtAsc(500L)).thenReturn(Collections.singletonList(attachment));

        List<AttachmentResponse> responses = attachmentService.getAttachments(10L, 100L, 500L, "dev@example.com");

        assertEquals(1, responses.size());
        assertEquals("architecture.png", responses.get(0).getFileName());
    }

    @Test
    void testDownloadAttachment_Success() {
        Resource mockResource = new ByteArrayResource("test content".getBytes());
        when(userRepository.findByEmail("dev@example.com")).thenReturn(Optional.of(devUser));
        when(projectService.getProjectEntity(10L, 100L)).thenReturn(project);
        when(taskRepository.findByIdAndProjectId(500L, 100L)).thenReturn(Optional.of(task));
        when(attachmentRepository.findByIdAndTaskId(2000L, 500L)).thenReturn(Optional.of(attachment));
        when(storageService.loadAsResource("uuid_architecture.png")).thenReturn(mockResource);

        AttachmentService.AttachmentDownload download = attachmentService.getAttachmentDownload(10L, 100L, 500L, 2000L, "dev@example.com");

        assertNotNull(download);
        assertEquals("architecture.png", download.getAttachment().getFileName());
        assertEquals(mockResource, download.getResource());
    }

    @Test
    void testDeleteAttachment_ByUploader_Success() {
        when(userRepository.findByEmail("dev@example.com")).thenReturn(Optional.of(devUser));
        when(projectService.getProjectEntity(10L, 100L)).thenReturn(project);
        when(taskRepository.findByIdAndProjectId(500L, 100L)).thenReturn(Optional.of(task));
        when(attachmentRepository.findByIdAndTaskId(2000L, 500L)).thenReturn(Optional.of(attachment));

        attachmentService.deleteAttachment(10L, 100L, 500L, 2000L, "dev@example.com");

        verify(attachmentRepository).delete(attachment);
        verify(storageService).delete("uuid_architecture.png");
    }

    @Test
    void testDeleteAttachment_ByUnauthorizedUser_ThrowsException() {
        User anotherDev = new User(4L, "anotherdev@example.com", "password", Role.DEVELOPER, null);
        when(userRepository.findByEmail("anotherdev@example.com")).thenReturn(Optional.of(anotherDev));
        when(projectService.getProjectEntity(10L, 100L)).thenReturn(project);
        when(taskRepository.findByIdAndProjectId(500L, 100L)).thenReturn(Optional.of(task));
        when(attachmentRepository.findByIdAndTaskId(2000L, 500L)).thenReturn(Optional.of(attachment));
        when(organizationMemberRepository.findByOrganizationIdAndUserId(10L, 4L)).thenReturn(Optional.empty());
        when(projectMemberRepository.findByProjectIdAndUserId(100L, 4L)).thenReturn(Optional.empty());

        assertThrows(InsufficientPermissionsException.class,
                () -> attachmentService.deleteAttachment(10L, 100L, 500L, 2000L, "anotherdev@example.com"));
    }
}

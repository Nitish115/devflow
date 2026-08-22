package com.devflow.devflow.comment;

import com.devflow.devflow.auth.Role;
import com.devflow.devflow.auth.User;
import com.devflow.devflow.auth.UserRepository;
import com.devflow.devflow.comment.dto.CommentResponse;
import com.devflow.devflow.comment.dto.CreateCommentRequest;
import com.devflow.devflow.comment.dto.UpdateCommentRequest;
import com.devflow.devflow.common.exception.InsufficientPermissionsException;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentMentionRepository commentMentionRepository;

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

    @InjectMocks
    private CommentService commentService;

    private User ownerUser;
    private User devUser;
    private User outsiderUser;
    private Organization organization;
    private Project project;
    private Task task;
    private Comment comment;

    @BeforeEach
    void setUp() {
        ownerUser = new User(1L, "owner@example.com", "password", Role.ORGANIZATION_OWNER, null);
        devUser = new User(2L, "dev@example.com", "password", Role.DEVELOPER, null);
        outsiderUser = new User(3L, "outsider@example.com", "password", Role.VIEWER, null);

        organization = new Organization(10L, "Acme Corp", "Tech", ownerUser);
        project = new Project(100L, "DevFlow Portal", "PM App", organization, ProjectStatus.ACTIVE, ownerUser);
        task = new Task(500L, project, "Setup DB", "Flyway setup", TaskStatus.TODO, TaskPriority.HIGH, devUser, ownerUser, LocalDate.now().plusDays(7));
        comment = new Comment(1000L, task, devUser, "Working on it!");
    }

    @Test
    void testCreateComment_Success_WithMemberMention() {
        CreateCommentRequest request = new CreateCommentRequest("Hey @owner@example.com check this task out");

        when(userRepository.findByEmail("dev@example.com")).thenReturn(Optional.of(devUser));
        when(projectService.getProjectEntity(10L, 100L)).thenReturn(project);
        when(taskRepository.findByIdAndProjectId(500L, 100L)).thenReturn(Optional.of(task));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(ownerUser));
        when(projectMemberRepository.existsByProjectIdAndUserId(100L, 1L)).thenReturn(true);

        CommentResponse response = commentService.createComment(10L, 100L, 500L, request, "dev@example.com");

        assertNotNull(response);
        assertEquals(1000L, response.getId());
        assertEquals(1, response.getMentionedUserIds().size());
        assertEquals(1L, response.getMentionedUserIds().get(0));

        verify(commentMentionRepository).save(any(CommentMention.class));
    }

    @Test
    void testCreateComment_SilentlyIgnoreNonMemberMention() {
        CreateCommentRequest request = new CreateCommentRequest("Hey @outsider@example.com take a look");

        when(userRepository.findByEmail("dev@example.com")).thenReturn(Optional.of(devUser));
        when(projectService.getProjectEntity(10L, 100L)).thenReturn(project);
        when(taskRepository.findByIdAndProjectId(500L, 100L)).thenReturn(Optional.of(task));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(userRepository.findByEmail("outsider@example.com")).thenReturn(Optional.of(outsiderUser));
        when(projectMemberRepository.existsByProjectIdAndUserId(100L, 3L)).thenReturn(false);

        CommentResponse response = commentService.createComment(10L, 100L, 500L, request, "dev@example.com");

        assertNotNull(response);
        assertTrue(response.getMentionedUserIds().isEmpty());
        verify(commentMentionRepository, never()).save(any(CommentMention.class));
    }

    @Test
    void testGetComments_Success() {
        when(userRepository.findByEmail("dev@example.com")).thenReturn(Optional.of(devUser));
        when(projectService.getProjectEntity(10L, 100L)).thenReturn(project);
        when(taskRepository.findByIdAndProjectId(500L, 100L)).thenReturn(Optional.of(task));
        when(commentRepository.findAllByTaskIdOrderByCreatedAtAsc(500L)).thenReturn(Collections.singletonList(comment));
        when(commentMentionRepository.findAllByCommentId(1000L)).thenReturn(Collections.emptyList());

        List<CommentResponse> responses = commentService.getComments(10L, 100L, 500L, "dev@example.com");

        assertEquals(1, responses.size());
        assertEquals("Working on it!", responses.get(0).getContent());
    }

    @Test
    void testUpdateComment_ByAuthor_Success() {
        UpdateCommentRequest request = new UpdateCommentRequest("Updated content!");

        when(userRepository.findByEmail("dev@example.com")).thenReturn(Optional.of(devUser));
        when(projectService.getProjectEntity(10L, 100L)).thenReturn(project);
        when(taskRepository.findByIdAndProjectId(500L, 100L)).thenReturn(Optional.of(task));
        when(commentRepository.findByIdAndTaskId(1000L, 500L)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentResponse response = commentService.updateComment(10L, 100L, 500L, 1000L, request, "dev@example.com");

        assertNotNull(response);
        verify(commentRepository).save(comment);
        verify(commentMentionRepository).deleteAllByCommentId(1000L);
    }

    @Test
    void testUpdateComment_ByNonAuthor_ThrowsException() {
        UpdateCommentRequest request = new UpdateCommentRequest("Hacked content!");

        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(ownerUser));
        when(projectService.getProjectEntity(10L, 100L)).thenReturn(project);
        when(taskRepository.findByIdAndProjectId(500L, 100L)).thenReturn(Optional.of(task));
        when(commentRepository.findByIdAndTaskId(1000L, 500L)).thenReturn(Optional.of(comment));

        assertThrows(InsufficientPermissionsException.class,
                () -> commentService.updateComment(10L, 100L, 500L, 1000L, request, "owner@example.com"));
    }

    @Test
    void testDeleteComment_ByAuthor_Success() {
        when(userRepository.findByEmail("dev@example.com")).thenReturn(Optional.of(devUser));
        when(projectService.getProjectEntity(10L, 100L)).thenReturn(project);
        when(taskRepository.findByIdAndProjectId(500L, 100L)).thenReturn(Optional.of(task));
        when(commentRepository.findByIdAndTaskId(1000L, 500L)).thenReturn(Optional.of(comment));

        commentService.deleteComment(10L, 100L, 500L, 1000L, "dev@example.com");

        verify(commentRepository).delete(comment);
    }

    @Test
    void testDeleteComment_ByOrgOwner_Success() {
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(ownerUser));
        when(projectService.getProjectEntity(10L, 100L)).thenReturn(project);
        when(taskRepository.findByIdAndProjectId(500L, 100L)).thenReturn(Optional.of(task));
        when(commentRepository.findByIdAndTaskId(1000L, 500L)).thenReturn(Optional.of(comment));
        when(organizationMemberRepository.findByOrganizationIdAndUserId(10L, 1L))
                .thenReturn(Optional.of(new com.devflow.devflow.organization.OrganizationMember(organization, ownerUser, Role.ORGANIZATION_OWNER)));

        commentService.deleteComment(10L, 100L, 500L, 1000L, "owner@example.com");

        verify(commentRepository).delete(comment);
    }

    @Test
    void testDeleteComment_ByUnauthorizedUser_ThrowsException() {
        User anotherDev = new User(4L, "anotherdev@example.com", "password", Role.DEVELOPER, null);
        when(userRepository.findByEmail("anotherdev@example.com")).thenReturn(Optional.of(anotherDev));
        when(projectService.getProjectEntity(10L, 100L)).thenReturn(project);
        when(taskRepository.findByIdAndProjectId(500L, 100L)).thenReturn(Optional.of(task));
        when(commentRepository.findByIdAndTaskId(1000L, 500L)).thenReturn(Optional.of(comment));
        when(organizationMemberRepository.findByOrganizationIdAndUserId(10L, 4L)).thenReturn(Optional.empty());
        when(projectMemberRepository.findByProjectIdAndUserId(100L, 4L)).thenReturn(Optional.empty());

        assertThrows(InsufficientPermissionsException.class,
                () -> commentService.deleteComment(10L, 100L, 500L, 1000L, "anotherdev@example.com"));
    }
}

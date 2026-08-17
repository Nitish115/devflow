package com.devflow.devflow.task;

import com.devflow.devflow.auth.Role;
import com.devflow.devflow.auth.User;
import com.devflow.devflow.auth.UserRepository;
import com.devflow.devflow.common.exception.*;
import com.devflow.devflow.organization.Organization;
import com.devflow.devflow.organization.OrganizationMember;
import com.devflow.devflow.organization.OrganizationMemberRepository;
import com.devflow.devflow.project.*;
import com.devflow.devflow.task.dto.*;
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
class TaskServiceTest {

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
    private TaskService taskService;

    private User ownerUser;
    private User devUser;
    private User nonMemberUser;
    private Organization organization;
    private Project project;
    private Task task;

    @BeforeEach
    void setUp() {
        ownerUser = new User(1L, "owner@example.com", "password", Role.ORGANIZATION_OWNER, null);
        devUser = new User(2L, "dev@example.com", "password", Role.DEVELOPER, null);
        nonMemberUser = new User(3L, "outsider@example.com", "password", Role.VIEWER, null);

        organization = new Organization(10L, "Acme Corp", "Tech", ownerUser);
        project = new Project(100L, "DevFlow Portal", "PM App", organization, ProjectStatus.ACTIVE, ownerUser);
        task = new Task(500L, project, "Setup DB", "Flyway setup", TaskStatus.TODO, TaskPriority.HIGH, devUser, ownerUser, LocalDate.now().plusDays(7));
    }

    @Test
    void testCreateTask_Success() {
        CreateTaskRequest request = new CreateTaskRequest("Setup DB", "Flyway setup", TaskPriority.HIGH, 2L, LocalDate.now().plusDays(7));

        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(ownerUser));
        when(projectService.getProjectEntity(10L, 100L)).thenReturn(project);
        when(userRepository.findById(2L)).thenReturn(Optional.of(devUser));
        when(projectMemberRepository.existsByProjectIdAndUserId(100L, 2L)).thenReturn(true);
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskResponse response = taskService.createTask(10L, 100L, request, "owner@example.com");

        assertNotNull(response);
        assertEquals(500L, response.getId());
        assertEquals("Setup DB", response.getTitle());
        assertEquals(2L, response.getAssigneeId());

        verify(projectService).validateOrgMembership(10L, 1L);
        verify(projectService).validateProjectMembership(100L, 1L);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void testCreateTask_AssigneeNotProjectMember_ThrowsException() {
        CreateTaskRequest request = new CreateTaskRequest("Setup DB", "Flyway setup", TaskPriority.HIGH, 3L, LocalDate.now().plusDays(7));

        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(ownerUser));
        when(projectService.getProjectEntity(10L, 100L)).thenReturn(project);
        when(userRepository.findById(3L)).thenReturn(Optional.of(nonMemberUser));
        when(projectMemberRepository.existsByProjectIdAndUserId(100L, 3L)).thenReturn(false);

        assertThrows(UserNotProjectMemberException.class,
                () -> taskService.createTask(10L, 100L, request, "owner@example.com"));
    }

    @Test
    void testGetTasks_FilterByStatusAndAssignee_Success() {
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(ownerUser));
        when(projectService.getProjectEntity(10L, 100L)).thenReturn(project);
        when(taskRepository.findAllByProjectIdAndStatusAndAssigneeId(100L, TaskStatus.TODO, 2L))
                .thenReturn(Collections.singletonList(task));

        List<TaskResponse> responses = taskService.getTasks(10L, 100L, TaskStatus.TODO, 2L, "owner@example.com");

        assertEquals(1, responses.size());
        assertEquals("Setup DB", responses.get(0).getTitle());
    }

    @Test
    void testGetTaskById_Success() {
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(ownerUser));
        when(projectService.getProjectEntity(10L, 100L)).thenReturn(project);
        when(taskRepository.findByIdAndProjectId(500L, 100L)).thenReturn(Optional.of(task));

        TaskResponse response = taskService.getTaskById(10L, 100L, 500L, "owner@example.com");

        assertNotNull(response);
        assertEquals(500L, response.getId());
    }

    @Test
    void testUpdateTask_StatusTransitionAndReassign_Success() {
        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setStatus(TaskStatus.IN_PROGRESS);
        request.setAssigneeId(2L);

        when(userRepository.findByEmail("dev@example.com")).thenReturn(Optional.of(devUser));
        when(projectService.getProjectEntity(10L, 100L)).thenReturn(project);
        when(taskRepository.findByIdAndProjectId(500L, 100L)).thenReturn(Optional.of(task));
        when(userRepository.findById(2L)).thenReturn(Optional.of(devUser));
        when(projectMemberRepository.existsByProjectIdAndUserId(100L, 2L)).thenReturn(true);
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskResponse response = taskService.updateTask(10L, 100L, 500L, request, "dev@example.com");

        assertNotNull(response);
        verify(taskRepository).save(task);
    }

    @Test
    void testUpdateTask_ReassignToNonProjectMember_ThrowsException() {
        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setAssigneeId(3L);

        when(userRepository.findByEmail("dev@example.com")).thenReturn(Optional.of(devUser));
        when(projectService.getProjectEntity(10L, 100L)).thenReturn(project);
        when(taskRepository.findByIdAndProjectId(500L, 100L)).thenReturn(Optional.of(task));
        when(userRepository.findById(3L)).thenReturn(Optional.of(nonMemberUser));
        when(projectMemberRepository.existsByProjectIdAndUserId(100L, 3L)).thenReturn(false);

        assertThrows(UserNotProjectMemberException.class,
                () -> taskService.updateTask(10L, 100L, 500L, request, "dev@example.com"));
    }

    @Test
    void testDeleteTask_ByCreator_Success() {
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(ownerUser));
        when(projectService.getProjectEntity(10L, 100L)).thenReturn(project);
        when(taskRepository.findByIdAndProjectId(500L, 100L)).thenReturn(Optional.of(task));

        taskService.deleteTask(10L, 100L, 500L, "owner@example.com");

        verify(taskRepository).delete(task);
    }

    @Test
    void testDeleteTask_ByNonCreatorDev_ThrowsException() {
        when(userRepository.findByEmail("dev@example.com")).thenReturn(Optional.of(devUser));
        when(projectService.getProjectEntity(10L, 100L)).thenReturn(project);
        when(taskRepository.findByIdAndProjectId(500L, 100L)).thenReturn(Optional.of(task));
        when(organizationMemberRepository.findByOrganizationIdAndUserId(10L, 2L)).thenReturn(Optional.empty());
        when(projectMemberRepository.findByProjectIdAndUserId(100L, 2L)).thenReturn(Optional.empty());

        assertThrows(InsufficientPermissionsException.class,
                () -> taskService.deleteTask(10L, 100L, 500L, "dev@example.com"));
    }
}

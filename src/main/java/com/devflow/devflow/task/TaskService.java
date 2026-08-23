package com.devflow.devflow.task;

import com.devflow.devflow.auth.Role;
import com.devflow.devflow.auth.User;
import com.devflow.devflow.auth.UserRepository;
import com.devflow.devflow.common.exception.*;
import com.devflow.devflow.event.EventPublisher;
import com.devflow.devflow.event.TaskAssignedEvent;
import com.devflow.devflow.event.TaskStatusChangedEvent;
import com.devflow.devflow.organization.OrganizationMember;
import com.devflow.devflow.organization.OrganizationMemberRepository;
import com.devflow.devflow.project.Project;
import com.devflow.devflow.project.ProjectMember;
import com.devflow.devflow.project.ProjectMemberRepository;
import com.devflow.devflow.project.ProjectService;
import com.devflow.devflow.task.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectService projectService;
    private final ProjectMemberRepository projectMemberRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final UserRepository userRepository;
    private final EventPublisher eventPublisher;

    public TaskService(TaskRepository taskRepository,
                       ProjectService projectService,
                       ProjectMemberRepository projectMemberRepository,
                       OrganizationMemberRepository organizationMemberRepository,
                       UserRepository userRepository,
                       EventPublisher eventPublisher) {
        this.taskRepository = taskRepository;
        this.projectService = projectService;
        this.projectMemberRepository = projectMemberRepository;
        this.organizationMemberRepository = organizationMemberRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public TaskResponse createTask(Long orgId, Long projectId, CreateTaskRequest request, String userEmail) {
        User currentUser = getUserByEmail(userEmail);
        projectService.validateOrgMembership(orgId, currentUser.getId());
        Project project = projectService.getProjectEntity(orgId, projectId);
        projectService.validateProjectMembership(projectId, currentUser.getId());

        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = validateAndGetAssignee(projectId, request.getAssigneeId());
        }

        Task task = new Task(
                project,
                request.getTitle(),
                request.getDescription(),
                TaskStatus.TODO,
                request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM,
                assignee,
                currentUser,
                request.getDueDate()
        );

        Task savedTask = taskRepository.save(task);

        if (savedTask.getAssignee() != null) {
            eventPublisher.publishTaskAssigned(new TaskAssignedEvent(
                    savedTask.getId(),
                    projectId,
                    orgId,
                    savedTask.getTitle(),
                    savedTask.getAssignee().getId(),
                    currentUser.getId(),
                    currentUser.getEmail()
            ));
        }

        return mapToTaskResponse(savedTask);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasks(Long orgId, Long projectId, TaskStatus status, Long assigneeId, String userEmail) {
        User currentUser = getUserByEmail(userEmail);
        projectService.validateOrgMembership(orgId, currentUser.getId());
        projectService.getProjectEntity(orgId, projectId);
        projectService.validateProjectMembership(projectId, currentUser.getId());

        List<Task> tasks;
        if (status != null && assigneeId != null) {
            tasks = taskRepository.findAllByProjectIdAndStatusAndAssigneeId(projectId, status, assigneeId);
        } else if (status != null) {
            tasks = taskRepository.findAllByProjectIdAndStatus(projectId, status);
        } else if (assigneeId != null) {
            tasks = taskRepository.findAllByProjectIdAndAssigneeId(projectId, assigneeId);
        } else {
            tasks = taskRepository.findAllByProjectId(projectId);
        }

        return tasks.stream()
                .map(this::mapToTaskResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long orgId, Long projectId, Long taskId, String userEmail) {
        User currentUser = getUserByEmail(userEmail);
        projectService.validateOrgMembership(orgId, currentUser.getId());
        projectService.getProjectEntity(orgId, projectId);
        projectService.validateProjectMembership(projectId, currentUser.getId());

        Task task = getTaskEntity(projectId, taskId);
        return mapToTaskResponse(task);
    }

    @Transactional
    public TaskResponse updateTask(Long orgId, Long projectId, Long taskId, UpdateTaskRequest request, String userEmail) {
        User currentUser = getUserByEmail(userEmail);
        projectService.validateOrgMembership(orgId, currentUser.getId());
        projectService.getProjectEntity(orgId, projectId);
        projectService.validateProjectMembership(projectId, currentUser.getId());

        Task task = getTaskEntity(projectId, taskId);
        TaskStatus oldStatus = task.getStatus();
        Long oldAssigneeId = task.getAssignee() != null ? task.getAssignee().getId() : null;

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }
        if (request.getAssigneeId() != null) {
            User assignee = validateAndGetAssignee(projectId, request.getAssigneeId());
            task.setAssignee(assignee);
        }

        Task updatedTask = taskRepository.save(task);

        Long newAssigneeId = updatedTask.getAssignee() != null ? updatedTask.getAssignee().getId() : null;
        if (newAssigneeId != null && !newAssigneeId.equals(oldAssigneeId)) {
            eventPublisher.publishTaskAssigned(new TaskAssignedEvent(
                    updatedTask.getId(),
                    projectId,
                    orgId,
                    updatedTask.getTitle(),
                    newAssigneeId,
                    currentUser.getId(),
                    currentUser.getEmail()
            ));
        }

        if (request.getStatus() != null && !oldStatus.equals(updatedTask.getStatus())) {
            eventPublisher.publishTaskStatusChanged(new TaskStatusChangedEvent(
                    updatedTask.getId(),
                    projectId,
                    orgId,
                    updatedTask.getTitle(),
                    oldStatus.name(),
                    updatedTask.getStatus().name(),
                    currentUser.getId(),
                    currentUser.getEmail(),
                    newAssigneeId,
                    updatedTask.getCreatedBy().getId()
            ));
        }

        return mapToTaskResponse(updatedTask);
    }


    @Transactional
    public void deleteTask(Long orgId, Long projectId, Long taskId, String userEmail) {
        User currentUser = getUserByEmail(userEmail);
        projectService.validateOrgMembership(orgId, currentUser.getId());
        projectService.getProjectEntity(orgId, projectId);
        projectService.validateProjectMembership(projectId, currentUser.getId());

        Task task = getTaskEntity(projectId, taskId);

        boolean isCreator = task.getCreatedBy().getId().equals(currentUser.getId());
        boolean isElevated = canManageProjectOrOrg(orgId, projectId, currentUser.getId());

        if (!isCreator && !isElevated) {
            throw new InsufficientPermissionsException("Only the task creator, Project Manager, or Organization Owner/Admin can delete this task");
        }

        taskRepository.delete(task);
    }

    private User validateAndGetAssignee(Long projectId, Long assigneeId) {
        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new UserNotFoundException("Assignee user not found with id: " + assigneeId));

        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, assigneeId)) {
            throw new UserNotProjectMemberException("Assignee must be an active project member");
        }

        return assignee;
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

    private TaskResponse mapToTaskResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getProject().getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getAssignee() != null ? task.getAssignee().getId() : null,
                task.getAssignee() != null ? task.getAssignee().getEmail() : null,
                task.getCreatedBy().getId(),
                task.getCreatedBy().getEmail(),
                task.getDueDate(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}

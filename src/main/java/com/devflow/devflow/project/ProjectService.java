package com.devflow.devflow.project;

import com.devflow.devflow.auth.Role;
import com.devflow.devflow.auth.User;
import com.devflow.devflow.auth.UserRepository;
import com.devflow.devflow.common.exception.*;
import com.devflow.devflow.organization.Organization;
import com.devflow.devflow.organization.OrganizationMember;
import com.devflow.devflow.organization.OrganizationMemberRepository;
import com.devflow.devflow.organization.OrganizationRepository;
import com.devflow.devflow.project.dto.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository,
                          ProjectMemberRepository projectMemberRepository,
                          OrganizationRepository organizationRepository,
                          OrganizationMemberRepository organizationMemberRepository,
                          UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.organizationRepository = organizationRepository;
        this.organizationMemberRepository = organizationMemberRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    @CacheEvict(value = "projects", allEntries = true)
    public ProjectResponse createProject(Long orgId, CreateProjectRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);
        Organization org = getOrganizationEntity(orgId);

        validateOrgMembership(orgId, user.getId());

        Project project = new Project(
                request.getName(),
                request.getDescription(),
                org,
                request.getStatus(),
                user
        );
        Project savedProject = projectRepository.save(project);

        // Creator auto-added as project member (Role.PROJECT_MANAGER)
        ProjectMember creatorMember = new ProjectMember(savedProject, user, Role.PROJECT_MANAGER);
        projectMemberRepository.save(creatorMember);

        return mapToProjectResponse(savedProject);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "projects", key = "'org:' + #orgId + ':user:' + #userEmail")
    public List<ProjectResponse> getOrganizationProjects(Long orgId, String userEmail) {
        User user = getUserByEmail(userEmail);
        getOrganizationEntity(orgId);

        validateOrgMembership(orgId, user.getId());

        List<Project> projects = projectRepository.findAllByOrganizationId(orgId);
        return projects.stream()
                .map(this::mapToProjectResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "projects", key = "'id:' + #projectId + ':user:' + #userEmail")
    public ProjectResponse getProjectById(Long orgId, Long projectId, String userEmail) {
        User user = getUserByEmail(userEmail);
        getOrganizationEntity(orgId);

        validateOrgMembership(orgId, user.getId());

        Project project = getProjectEntity(orgId, projectId);
        validateProjectMembership(projectId, user.getId());

        return mapToProjectResponse(project);
    }

    @Transactional
    @CacheEvict(value = "projects", allEntries = true)
    public ProjectMemberResponse addProjectMember(Long orgId, Long projectId, AddProjectMemberRequest request, String userEmail) {
        User currentUser = getUserByEmail(userEmail);
        getOrganizationEntity(orgId);

        validateOrgMembership(orgId, currentUser.getId());

        Project project = getProjectEntity(orgId, projectId);
        validateProjectManagerOrOrgAdminPermissions(orgId, projectId, currentUser.getId());

        User targetUser;
        if (request.getUserId() != null) {
            targetUser = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + request.getUserId()));
        } else if (request.getEmail() != null && !request.getEmail().isBlank()) {
            targetUser = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UserNotFoundException("User not found with email: " + request.getEmail()));
        } else {
            throw new InvalidOperationException("Either userId or email must be provided");
        }

        // Validate target user is a member of the organization
        if (!organizationMemberRepository.existsByOrganizationIdAndUserId(orgId, targetUser.getId())) {
            throw new UserNotMemberException("User must be an organization member before being added to a project");
        }

        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, targetUser.getId())) {
            throw new MemberAlreadyExistsException("User is already a member of this project");
        }

        ProjectMember member = new ProjectMember(project, targetUser, request.getRole());
        ProjectMember savedMember = projectMemberRepository.save(member);

        return mapToProjectMemberResponse(savedMember);
    }

    @Transactional
    @CacheEvict(value = "projects", allEntries = true)
    public void removeProjectMember(Long orgId, Long projectId, Long targetUserId, String userEmail) {
        User currentUser = getUserByEmail(userEmail);
        getOrganizationEntity(orgId);

        validateOrgMembership(orgId, currentUser.getId());

        getProjectEntity(orgId, projectId);
        validateProjectManagerOrOrgAdminPermissions(orgId, projectId, currentUser.getId());

        ProjectMember targetMember = projectMemberRepository.findByProjectIdAndUserId(projectId, targetUserId)
                .orElseThrow(() -> new UserNotProjectMemberException("User is not a member of this project"));

        projectMemberRepository.delete(targetMember);
    }


    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    private Organization getOrganizationEntity(Long orgId) {
        return organizationRepository.findById(orgId)
                .orElseThrow(() -> new OrganizationNotFoundException("Organization not found with id: " + orgId));
    }

    public Project getProjectEntity(Long orgId, Long projectId) {
        return projectRepository.findByIdAndOrganizationId(projectId, orgId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId + " in organization: " + orgId));
    }

    public void validateOrgMembership(Long orgId, Long userId) {
        if (!organizationMemberRepository.existsByOrganizationIdAndUserId(orgId, userId)) {
            throw new UserNotMemberException("User is not a member of this organization");
        }
    }

    public void validateProjectMembership(Long projectId, Long userId) {
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new UserNotProjectMemberException("User is not a member of this project");
        }
    }

    public void validateProjectManagerOrOrgAdminPermissions(Long orgId, Long projectId, Long userId) {
        Optional<OrganizationMember> orgMember = organizationMemberRepository.findByOrganizationIdAndUserId(orgId, userId);
        if (orgMember.isPresent() && (orgMember.get().getRole() == Role.ORGANIZATION_OWNER || orgMember.get().getRole() == Role.ORGANIZATION_ADMIN)) {
            return;
        }

        Optional<ProjectMember> projectMember = projectMemberRepository.findByProjectIdAndUserId(projectId, userId);
        if (projectMember.isPresent() && projectMember.get().getRole() == Role.PROJECT_MANAGER) {
            return;
        }

        throw new InsufficientPermissionsException("Only Project Managers or Organization Owners/Admins can perform this action");
    }

    private ProjectResponse mapToProjectResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getOrganization().getId(),
                project.getStatus(),
                project.getCreatedBy().getId(),
                project.getCreatedBy().getEmail(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    private ProjectMemberResponse mapToProjectMemberResponse(ProjectMember member) {
        return new ProjectMemberResponse(
                member.getId(),
                member.getProject().getId(),
                member.getUser().getId(),
                member.getUser().getEmail(),
                member.getRole(),
                member.getCreatedAt()
        );
    }
}

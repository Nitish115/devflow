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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationMemberRepository organizationMemberRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProjectService projectService;

    private User ownerUser;
    private User devUser;
    private Organization organization;
    private Project project;
    private ProjectMember pmMember;
    private ProjectMember devProjectMember;

    @BeforeEach
    void setUp() {
        ownerUser = new User(1L, "owner@example.com", "password", Role.ORGANIZATION_OWNER, null);
        devUser = new User(2L, "dev@example.com", "password", Role.DEVELOPER, null);
        organization = new Organization(10L, "Acme Corp", "Tech", ownerUser);
        project = new Project(100L, "DevFlow Portal", "PM App", organization, ProjectStatus.ACTIVE, ownerUser);

        pmMember = new ProjectMember(1000L, project, ownerUser, Role.PROJECT_MANAGER);
        devProjectMember = new ProjectMember(1001L, project, devUser, Role.DEVELOPER);
    }

    @Test
    void testCreateProject_Success() {
        CreateProjectRequest request = new CreateProjectRequest("DevFlow Portal", "PM App");

        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(ownerUser));
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(organization));
        when(organizationMemberRepository.existsByOrganizationIdAndUserId(10L, 1L)).thenReturn(true);
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(projectMemberRepository.save(any(ProjectMember.class))).thenReturn(pmMember);

        ProjectResponse response = projectService.createProject(10L, request, "owner@example.com");

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("DevFlow Portal", response.getName());
        assertEquals("owner@example.com", response.getCreatedByEmail());

        verify(projectRepository).save(any(Project.class));
        verify(projectMemberRepository).save(any(ProjectMember.class));
    }

    @Test
    void testCreateProject_UserNotOrgMember_ThrowsException() {
        CreateProjectRequest request = new CreateProjectRequest("DevFlow Portal", "PM App");

        when(userRepository.findByEmail("dev@example.com")).thenReturn(Optional.of(devUser));
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(organization));
        when(organizationMemberRepository.existsByOrganizationIdAndUserId(10L, 2L)).thenReturn(false);

        assertThrows(UserNotMemberException.class,
                () -> projectService.createProject(10L, request, "dev@example.com"));
    }

    @Test
    void testGetOrganizationProjects_Success() {
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(ownerUser));
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(organization));
        when(organizationMemberRepository.existsByOrganizationIdAndUserId(10L, 1L)).thenReturn(true);
        when(projectRepository.findAllByOrganizationId(10L)).thenReturn(Collections.singletonList(project));

        List<ProjectResponse> responses = projectService.getOrganizationProjects(10L, "owner@example.com");

        assertEquals(1, responses.size());
        assertEquals("DevFlow Portal", responses.get(0).getName());
    }

    @Test
    void testGetProjectById_Success() {
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(ownerUser));
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(organization));
        when(organizationMemberRepository.existsByOrganizationIdAndUserId(10L, 1L)).thenReturn(true);
        when(projectRepository.findByIdAndOrganizationId(100L, 10L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProjectIdAndUserId(100L, 1L)).thenReturn(true);

        ProjectResponse response = projectService.getProjectById(10L, 100L, "owner@example.com");

        assertNotNull(response);
        assertEquals(100L, response.getId());
    }

    @Test
    void testGetProjectById_NotProjectMember_ThrowsException() {
        when(userRepository.findByEmail("dev@example.com")).thenReturn(Optional.of(devUser));
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(organization));
        when(organizationMemberRepository.existsByOrganizationIdAndUserId(10L, 2L)).thenReturn(true);
        when(projectRepository.findByIdAndOrganizationId(100L, 10L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProjectIdAndUserId(100L, 2L)).thenReturn(false);

        assertThrows(UserNotProjectMemberException.class,
                () -> projectService.getProjectById(10L, 100L, "dev@example.com"));
    }

    @Test
    void testAddProjectMember_Success() {
        AddProjectMemberRequest request = new AddProjectMemberRequest("dev@example.com", Role.DEVELOPER);

        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(ownerUser));
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(organization));
        when(organizationMemberRepository.existsByOrganizationIdAndUserId(10L, 1L)).thenReturn(true);
        when(projectRepository.findByIdAndOrganizationId(100L, 10L)).thenReturn(Optional.of(project));

        OrganizationMember orgOwnerMember = new OrganizationMember(organization, ownerUser, Role.ORGANIZATION_OWNER);
        when(organizationMemberRepository.findByOrganizationIdAndUserId(10L, 1L)).thenReturn(Optional.of(orgOwnerMember));

        when(userRepository.findByEmail("dev@example.com")).thenReturn(Optional.of(devUser));
        when(organizationMemberRepository.existsByOrganizationIdAndUserId(10L, 2L)).thenReturn(true);
        when(projectMemberRepository.existsByProjectIdAndUserId(100L, 2L)).thenReturn(false);
        when(projectMemberRepository.save(any(ProjectMember.class))).thenReturn(devProjectMember);

        ProjectMemberResponse response = projectService.addProjectMember(10L, 100L, request, "owner@example.com");

        assertNotNull(response);
        assertEquals(2L, response.getUserId());
        assertEquals(Role.DEVELOPER, response.getRole());
    }

    @Test
    void testAddProjectMember_TargetNotOrgMember_ThrowsException() {
        AddProjectMemberRequest request = new AddProjectMemberRequest("dev@example.com", Role.DEVELOPER);

        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(ownerUser));
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(organization));
        when(organizationMemberRepository.existsByOrganizationIdAndUserId(10L, 1L)).thenReturn(true);
        when(projectRepository.findByIdAndOrganizationId(100L, 10L)).thenReturn(Optional.of(project));

        OrganizationMember orgOwnerMember = new OrganizationMember(organization, ownerUser, Role.ORGANIZATION_OWNER);
        when(organizationMemberRepository.findByOrganizationIdAndUserId(10L, 1L)).thenReturn(Optional.of(orgOwnerMember));

        when(userRepository.findByEmail("dev@example.com")).thenReturn(Optional.of(devUser));
        when(organizationMemberRepository.existsByOrganizationIdAndUserId(10L, 2L)).thenReturn(false);

        assertThrows(UserNotMemberException.class,
                () -> projectService.addProjectMember(10L, 100L, request, "owner@example.com"));
    }

    @Test
    void testRemoveProjectMember_Success() {
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(ownerUser));
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(organization));
        when(organizationMemberRepository.existsByOrganizationIdAndUserId(10L, 1L)).thenReturn(true);
        when(projectRepository.findByIdAndOrganizationId(100L, 10L)).thenReturn(Optional.of(project));

        OrganizationMember orgOwnerMember = new OrganizationMember(organization, ownerUser, Role.ORGANIZATION_OWNER);
        when(organizationMemberRepository.findByOrganizationIdAndUserId(10L, 1L)).thenReturn(Optional.of(orgOwnerMember));
        when(projectMemberRepository.findByProjectIdAndUserId(100L, 2L)).thenReturn(Optional.of(devProjectMember));

        projectService.removeProjectMember(10L, 100L, 2L, "owner@example.com");

        verify(projectMemberRepository).delete(devProjectMember);
    }
}

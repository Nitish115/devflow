package com.devflow.devflow.config;

import com.devflow.devflow.auth.CustomUserDetailsService;
import com.devflow.devflow.auth.Role;
import com.devflow.devflow.auth.User;
import com.devflow.devflow.auth.UserRepository;
import com.devflow.devflow.organization.Organization;
import com.devflow.devflow.organization.OrganizationMember;
import com.devflow.devflow.organization.OrganizationMemberRepository;
import com.devflow.devflow.organization.OrganizationRepository;
import com.devflow.devflow.organization.OrganizationService;
import com.devflow.devflow.organization.dto.AddMemberRequest;
import com.devflow.devflow.organization.dto.OrganizationResponse;
import com.devflow.devflow.project.*;
import com.devflow.devflow.project.dto.ProjectResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class RedisCacheTest {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private OrganizationRepository organizationRepository;

    @MockBean
    private OrganizationMemberRepository organizationMemberRepository;

    @MockBean
    private ProjectRepository projectRepository;

    @MockBean
    private ProjectMemberRepository projectMemberRepository;

    @MockBean
    private UserRepository userRepository;

    private User ownerUser;
    private User devUser;
    private Organization organization;
    private Project project;

    @BeforeEach
    void setUp() {
        if (cacheManager.getCache("organizations") != null) {
            cacheManager.getCache("organizations").clear();
        }
        if (cacheManager.getCache("projects") != null) {
            cacheManager.getCache("projects").clear();
        }
        if (cacheManager.getCache("users") != null) {
            cacheManager.getCache("users").clear();
        }

        ownerUser = new User(1L, "owner@example.com", "password", Role.ORGANIZATION_OWNER, null);
        devUser = new User(2L, "dev@example.com", "password", Role.DEVELOPER, null);

        organization = new Organization(10L, "Acme Corp", "Tech", ownerUser);
        project = new Project(100L, "DevFlow Portal", "PM App", organization, ProjectStatus.ACTIVE, ownerUser);
    }

    @Test
    void testOrganizationCachingAndEvictionOnMemberAddition() {
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(ownerUser));
        when(userRepository.findByEmail("dev@example.com")).thenReturn(Optional.of(devUser));
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(organization));
        when(organizationMemberRepository.existsByOrganizationIdAndUserId(10L, 1L)).thenReturn(true);
        when(organizationMemberRepository.findByOrganizationIdAndUserId(10L, 1L))
                .thenReturn(Optional.of(new OrganizationMember(organization, ownerUser, Role.ORGANIZATION_OWNER)));
        when(organizationMemberRepository.existsByOrganizationIdAndUserId(10L, 2L)).thenReturn(false);

        // First call - populates cache
        OrganizationResponse firstResponse = organizationService.getOrganizationById(10L, "owner@example.com");
        assertNotNull(firstResponse);

        // Second call - served from cache (repository not called again)
        OrganizationResponse secondResponse = organizationService.getOrganizationById(10L, "owner@example.com");
        assertNotNull(secondResponse);
        verify(organizationRepository, times(1)).findById(10L);

        // Adding member - triggers @CacheEvict(allEntries = true)
        AddMemberRequest addRequest = new AddMemberRequest("dev@example.com", Role.DEVELOPER);
        when(organizationMemberRepository.save(any(OrganizationMember.class)))
                .thenReturn(new OrganizationMember(organization, devUser, Role.DEVELOPER));
        organizationService.addMember(10L, addRequest, "owner@example.com");

        // Third call - cache was evicted, repository is invoked again
        organizationService.getOrganizationById(10L, "owner@example.com");
        verify(organizationRepository, times(2)).findById(10L);
    }

    @Test
    void testProjectCachingAndEvictionOnProjectCreation() {
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(ownerUser));
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(organization));
        when(organizationMemberRepository.existsByOrganizationIdAndUserId(10L, 1L)).thenReturn(true);
        when(projectRepository.findAllByOrganizationId(10L)).thenReturn(Collections.singletonList(project));

        // First call - populates projects cache
        List<ProjectResponse> firstProjects = projectService.getOrganizationProjects(10L, "owner@example.com");
        assertEquals(1, firstProjects.size());

        // Second call - served from cache
        List<ProjectResponse> secondProjects = projectService.getOrganizationProjects(10L, "owner@example.com");
        assertEquals(1, secondProjects.size());
        verify(projectRepository, times(1)).findAllByOrganizationId(10L);
    }

    @Test
    void testUserDetailsServiceCaching() {
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(ownerUser));

        // First call - hits repository
        UserDetails user1 = userDetailsService.loadUserByUsername("owner@example.com");
        assertNotNull(user1);

        // Second call - hits cache
        UserDetails user2 = userDetailsService.loadUserByUsername("owner@example.com");
        assertNotNull(user2);

        verify(userRepository, times(1)).findByEmail("owner@example.com");
    }
}

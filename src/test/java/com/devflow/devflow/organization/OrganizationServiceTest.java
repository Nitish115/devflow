package com.devflow.devflow.organization;

import com.devflow.devflow.auth.Role;
import com.devflow.devflow.auth.User;
import com.devflow.devflow.auth.UserRepository;
import com.devflow.devflow.common.exception.*;
import com.devflow.devflow.organization.dto.*;
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
class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationMemberRepository organizationMemberRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrganizationService organizationService;

    private User ownerUser;
    private User devUser;
    private Organization organization;
    private OrganizationMember ownerMembership;
    private OrganizationMember devMembership;

    @BeforeEach
    void setUp() {
        ownerUser = new User(1L, "owner@example.com", "password", Role.ORGANIZATION_OWNER, null);
        devUser = new User(2L, "dev@example.com", "password", Role.DEVELOPER, null);
        organization = new Organization(10L, "Acme Corp", "Tech Solutions", ownerUser);

        ownerMembership = new OrganizationMember(100L, organization, ownerUser, Role.ORGANIZATION_OWNER);
        devMembership = new OrganizationMember(101L, organization, devUser, Role.DEVELOPER);
    }

    @Test
    void testCreateOrganization_Success() {
        CreateOrganizationRequest request = new CreateOrganizationRequest("Acme Corp", "Tech Solutions");

        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(ownerUser));
        when(organizationRepository.save(any(Organization.class))).thenReturn(organization);
        when(organizationMemberRepository.save(any(OrganizationMember.class))).thenReturn(ownerMembership);

        OrganizationResponse response = organizationService.createOrganization(request, "owner@example.com");

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Acme Corp", response.getName());
        assertEquals("owner@example.com", response.getOwnerEmail());

        verify(organizationRepository).save(any(Organization.class));
        verify(organizationMemberRepository).save(any(OrganizationMember.class));
    }

    @Test
    void testGetUserOrganizations_Success() {
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(ownerUser));
        when(organizationMemberRepository.findAllByUserId(1L)).thenReturn(Collections.singletonList(ownerMembership));

        List<OrganizationResponse> responses = organizationService.getUserOrganizations("owner@example.com");

        assertEquals(1, responses.size());
        assertEquals("Acme Corp", responses.get(0).getName());
    }

    @Test
    void testGetOrganizationById_Success() {
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(ownerUser));
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(organization));
        when(organizationMemberRepository.existsByOrganizationIdAndUserId(10L, 1L)).thenReturn(true);

        OrganizationResponse response = organizationService.getOrganizationById(10L, "owner@example.com");

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Acme Corp", response.getName());
    }

    @Test
    void testGetOrganizationById_UserNotMember_ThrowsException() {
        when(userRepository.findByEmail("dev@example.com")).thenReturn(Optional.of(devUser));
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(organization));
        when(organizationMemberRepository.existsByOrganizationIdAndUserId(10L, 2L)).thenReturn(false);

        assertThrows(UserNotMemberException.class, () -> organizationService.getOrganizationById(10L, "dev@example.com"));
    }

    @Test
    void testGetOrganizationById_OrgNotFound_ThrowsException() {
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(ownerUser));
        when(organizationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(OrganizationNotFoundException.class, () -> organizationService.getOrganizationById(999L, "owner@example.com"));
    }

    @Test
    void testAddMember_Success() {
        AddMemberRequest request = new AddMemberRequest("dev@example.com", Role.DEVELOPER);

        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(ownerUser));
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(organization));
        when(organizationMemberRepository.findByOrganizationIdAndUserId(10L, 1L)).thenReturn(Optional.of(ownerMembership));
        when(userRepository.findByEmail("dev@example.com")).thenReturn(Optional.of(devUser));
        when(organizationMemberRepository.existsByOrganizationIdAndUserId(10L, 2L)).thenReturn(false);
        when(organizationMemberRepository.save(any(OrganizationMember.class))).thenReturn(devMembership);

        OrganizationMemberResponse response = organizationService.addMember(10L, request, "owner@example.com");

        assertNotNull(response);
        assertEquals(2L, response.getUserId());
        assertEquals("dev@example.com", response.getUserEmail());
        assertEquals(Role.DEVELOPER, response.getRole());
    }

    @Test
    void testAddMember_InsufficientPermissions_ThrowsException() {
        AddMemberRequest request = new AddMemberRequest("other@example.com", Role.DEVELOPER);

        when(userRepository.findByEmail("dev@example.com")).thenReturn(Optional.of(devUser));
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(organization));
        when(organizationMemberRepository.findByOrganizationIdAndUserId(10L, 2L)).thenReturn(Optional.of(devMembership));

        assertThrows(InsufficientPermissionsException.class, () -> organizationService.addMember(10L, request, "dev@example.com"));
    }

    @Test
    void testAddMember_AlreadyMember_ThrowsException() {
        AddMemberRequest request = new AddMemberRequest("dev@example.com", Role.DEVELOPER);

        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(ownerUser));
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(organization));
        when(organizationMemberRepository.findByOrganizationIdAndUserId(10L, 1L)).thenReturn(Optional.of(ownerMembership));
        when(userRepository.findByEmail("dev@example.com")).thenReturn(Optional.of(devUser));
        when(organizationMemberRepository.existsByOrganizationIdAndUserId(10L, 2L)).thenReturn(true);

        assertThrows(MemberAlreadyExistsException.class, () -> organizationService.addMember(10L, request, "owner@example.com"));
    }

    @Test
    void testUpdateMemberRole_Success() {
        UpdateMemberRoleRequest request = new UpdateMemberRoleRequest(Role.ORGANIZATION_ADMIN);
        OrganizationMember updatedMember = new OrganizationMember(101L, organization, devUser, Role.ORGANIZATION_ADMIN);

        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(ownerUser));
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(organization));
        when(organizationMemberRepository.findByOrganizationIdAndUserId(10L, 1L)).thenReturn(Optional.of(ownerMembership));
        when(organizationMemberRepository.findByOrganizationIdAndUserId(10L, 2L)).thenReturn(Optional.of(devMembership));
        when(organizationMemberRepository.save(any(OrganizationMember.class))).thenReturn(updatedMember);

        OrganizationMemberResponse response = organizationService.updateMemberRole(10L, 2L, request, "owner@example.com");

        assertNotNull(response);
        assertEquals(Role.ORGANIZATION_ADMIN, response.getRole());
    }

    @Test
    void testUpdateMemberRole_DemoteLastOwner_ThrowsException() {
        UpdateMemberRoleRequest request = new UpdateMemberRoleRequest(Role.DEVELOPER);

        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(ownerUser));
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(organization));
        when(organizationMemberRepository.findByOrganizationIdAndUserId(10L, 1L)).thenReturn(Optional.of(ownerMembership));
        when(organizationMemberRepository.countByOrganizationIdAndRole(10L, Role.ORGANIZATION_OWNER)).thenReturn(1L);

        assertThrows(InvalidOperationException.class, () -> organizationService.updateMemberRole(10L, 1L, request, "owner@example.com"));
    }

    @Test
    void testRemoveMember_Success() {
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(ownerUser));
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(organization));
        when(organizationMemberRepository.findByOrganizationIdAndUserId(10L, 1L)).thenReturn(Optional.of(ownerMembership));
        when(organizationMemberRepository.findByOrganizationIdAndUserId(10L, 2L)).thenReturn(Optional.of(devMembership));

        organizationService.removeMember(10L, 2L, "owner@example.com");

        verify(organizationMemberRepository).delete(devMembership);
    }

    @Test
    void testRemoveMember_RemoveLastOwner_ThrowsException() {
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(ownerUser));
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(organization));
        when(organizationMemberRepository.findByOrganizationIdAndUserId(10L, 1L)).thenReturn(Optional.of(ownerMembership));
        when(organizationMemberRepository.countByOrganizationIdAndRole(10L, Role.ORGANIZATION_OWNER)).thenReturn(1L);

        assertThrows(InvalidOperationException.class, () -> organizationService.removeMember(10L, 1L, "owner@example.com"));
    }
}

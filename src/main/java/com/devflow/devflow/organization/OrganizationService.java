package com.devflow.devflow.organization;

import com.devflow.devflow.auth.Role;
import com.devflow.devflow.auth.User;
import com.devflow.devflow.auth.UserRepository;
import com.devflow.devflow.common.exception.*;
import com.devflow.devflow.organization.dto.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final UserRepository userRepository;

    public OrganizationService(OrganizationRepository organizationRepository,
                               OrganizationMemberRepository organizationMemberRepository,
                               UserRepository userRepository) {
        this.organizationRepository = organizationRepository;
        this.organizationMemberRepository = organizationMemberRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    @CacheEvict(value = "organizations", key = "'user:' + #userEmail")
    public OrganizationResponse createOrganization(CreateOrganizationRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);

        Organization organization = new Organization(request.getName(), request.getDescription(), user);
        Organization savedOrganization = organizationRepository.save(organization);

        OrganizationMember member = new OrganizationMember(savedOrganization, user, Role.ORGANIZATION_OWNER);
        organizationMemberRepository.save(member);

        return mapToOrganizationResponse(savedOrganization);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "organizations", key = "'user:' + #userEmail")
    public List<OrganizationResponse> getUserOrganizations(String userEmail) {
        User user = getUserByEmail(userEmail);

        List<OrganizationMember> memberships = organizationMemberRepository.findAllByUserId(user.getId());
        return memberships.stream()
                .map(m -> mapToOrganizationResponse(m.getOrganization()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "organizations", key = "'id:' + #orgId + ':user:' + #userEmail")
    public OrganizationResponse getOrganizationById(Long orgId, String userEmail) {
        User user = getUserByEmail(userEmail);
        Organization organization = getOrganizationEntity(orgId);

        boolean isMember = organizationMemberRepository.existsByOrganizationIdAndUserId(orgId, user.getId());
        if (!isMember) {
            throw new UserNotMemberException("User is not a member of this organization");
        }

        return mapToOrganizationResponse(organization);
    }

    @Transactional
    @CacheEvict(value = "organizations", allEntries = true)
    public OrganizationMemberResponse addMember(Long orgId, AddMemberRequest request, String userEmail) {
        User currentUser = getUserByEmail(userEmail);
        Organization organization = getOrganizationEntity(orgId);

        validateOwnerOrAdminPermissions(orgId, currentUser.getId());

        User targetUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + request.getEmail()));

        if (organizationMemberRepository.existsByOrganizationIdAndUserId(orgId, targetUser.getId())) {
            throw new MemberAlreadyExistsException("User is already a member of this organization");
        }

        OrganizationMember newMember = new OrganizationMember(organization, targetUser, request.getRole());
        OrganizationMember savedMember = organizationMemberRepository.save(newMember);

        return mapToMemberResponse(savedMember);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "organizations", allEntries = true),
            @CacheEvict(value = "projects", allEntries = true)
    })
    public OrganizationMemberResponse updateMemberRole(Long orgId, Long targetUserId, UpdateMemberRoleRequest request, String userEmail) {
        User currentUser = getUserByEmail(userEmail);
        getOrganizationEntity(orgId);

        validateOwnerOrAdminPermissions(orgId, currentUser.getId());

        OrganizationMember targetMember = organizationMemberRepository.findByOrganizationIdAndUserId(orgId, targetUserId)
                .orElseThrow(() -> new UserNotMemberException("Target user is not a member of this organization"));

        if (targetMember.getRole() == Role.ORGANIZATION_OWNER && request.getRole() != Role.ORGANIZATION_OWNER) {
            long ownerCount = organizationMemberRepository.countByOrganizationIdAndRole(orgId, Role.ORGANIZATION_OWNER);
            if (ownerCount <= 1) {
                throw new InvalidOperationException("Cannot demote the last remaining Organization Owner");
            }
        }

        targetMember.setRole(request.getRole());
        OrganizationMember updatedMember = organizationMemberRepository.save(targetMember);

        return mapToMemberResponse(updatedMember);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "organizations", allEntries = true),
            @CacheEvict(value = "projects", allEntries = true)
    })
    public void removeMember(Long orgId, Long targetUserId, String userEmail) {
        User currentUser = getUserByEmail(userEmail);
        getOrganizationEntity(orgId);

        validateOwnerOrAdminPermissions(orgId, currentUser.getId());

        OrganizationMember targetMember = organizationMemberRepository.findByOrganizationIdAndUserId(orgId, targetUserId)
                .orElseThrow(() -> new UserNotMemberException("Target user is not a member of this organization"));

        if (targetMember.getRole() == Role.ORGANIZATION_OWNER) {
            long ownerCount = organizationMemberRepository.countByOrganizationIdAndRole(orgId, Role.ORGANIZATION_OWNER);
            if (ownerCount <= 1) {
                throw new InvalidOperationException("Cannot remove the last remaining Organization Owner");
            }
        }

        organizationMemberRepository.delete(targetMember);
    }


    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    private Organization getOrganizationEntity(Long orgId) {
        return organizationRepository.findById(orgId)
                .orElseThrow(() -> new OrganizationNotFoundException("Organization not found with id: " + orgId));
    }

    private void validateOwnerOrAdminPermissions(Long orgId, Long userId) {
        OrganizationMember member = organizationMemberRepository.findByOrganizationIdAndUserId(orgId, userId)
                .orElseThrow(() -> new UserNotMemberException("User is not a member of this organization"));

        if (member.getRole() != Role.ORGANIZATION_OWNER && member.getRole() != Role.ORGANIZATION_ADMIN) {
            throw new InsufficientPermissionsException("Only Organization Owners and Admins can manage members");
        }
    }

    private OrganizationResponse mapToOrganizationResponse(Organization organization) {
        return new OrganizationResponse(
                organization.getId(),
                organization.getName(),
                organization.getDescription(),
                organization.getOwner().getId(),
                organization.getOwner().getEmail(),
                organization.getCreatedAt(),
                organization.getUpdatedAt()
        );
    }

    private OrganizationMemberResponse mapToMemberResponse(OrganizationMember member) {
        return new OrganizationMemberResponse(
                member.getId(),
                member.getOrganization().getId(),
                member.getUser().getId(),
                member.getUser().getEmail(),
                member.getRole(),
                member.getCreatedAt()
        );
    }
}

package com.devflow.devflow.organization;

import com.devflow.devflow.auth.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, Long> {

    Optional<OrganizationMember> findByOrganizationIdAndUserId(Long organizationId, Long userId);

    List<OrganizationMember> findAllByUserId(Long userId);

    boolean existsByOrganizationIdAndUserId(Long organizationId, Long userId);

    List<OrganizationMember> findAllByOrganizationId(Long organizationId);

    long countByOrganizationIdAndRole(Long organizationId, Role role);
}

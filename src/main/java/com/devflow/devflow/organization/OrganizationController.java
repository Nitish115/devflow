package com.devflow.devflow.organization;

import com.devflow.devflow.organization.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @PostMapping
    public ResponseEntity<OrganizationResponse> createOrganization(@Valid @RequestBody CreateOrganizationRequest request,
                                                                   Principal principal) {
        OrganizationResponse response = organizationService.createOrganization(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<OrganizationResponse>> getUserOrganizations(Principal principal) {
        List<OrganizationResponse> response = organizationService.getUserOrganizations(principal.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrganizationResponse> getOrganizationById(@PathVariable Long id,
                                                                     Principal principal) {
        OrganizationResponse response = organizationService.getOrganizationById(id, principal.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<OrganizationMemberResponse> addMember(@PathVariable Long id,
                                                                 @Valid @RequestBody AddMemberRequest request,
                                                                 Principal principal) {
        OrganizationMemberResponse response = organizationService.addMember(id, request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/members/{userId}")
    public ResponseEntity<OrganizationMemberResponse> updateMemberRole(@PathVariable Long id,
                                                                         @PathVariable Long userId,
                                                                         @Valid @RequestBody UpdateMemberRoleRequest request,
                                                                         Principal principal) {
        OrganizationMemberResponse response = organizationService.updateMemberRole(id, userId, request, principal.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<Void> removeMember(@PathVariable Long id,
                                             @PathVariable Long userId,
                                             Principal principal) {
        organizationService.removeMember(id, userId, principal.getName());
        return ResponseEntity.noContent().build();
    }
}

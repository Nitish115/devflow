package com.devflow.devflow.project;

import com.devflow.devflow.project.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/organizations/{orgId}/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@PathVariable Long orgId,
                                                         @Valid @RequestBody CreateProjectRequest request,
                                                         Principal principal) {
        ProjectResponse response = projectService.createProject(orgId, request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getOrganizationProjects(@PathVariable Long orgId,
                                                                         Principal principal) {
        List<ProjectResponse> response = projectService.getOrganizationProjects(orgId, principal.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable Long orgId,
                                                           @PathVariable Long projectId,
                                                           Principal principal) {
        ProjectResponse response = projectService.getProjectById(orgId, projectId, principal.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{projectId}/members")
    public ResponseEntity<ProjectMemberResponse> addProjectMember(@PathVariable Long orgId,
                                                                   @PathVariable Long projectId,
                                                                   @Valid @RequestBody AddProjectMemberRequest request,
                                                                   Principal principal) {
        ProjectMemberResponse response = projectService.addProjectMember(orgId, projectId, request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{projectId}/members/{userId}")
    public ResponseEntity<Void> removeProjectMember(@PathVariable Long orgId,
                                                     @PathVariable Long projectId,
                                                     @PathVariable Long userId,
                                                     Principal principal) {
        projectService.removeProjectMember(orgId, projectId, userId, principal.getName());
        return ResponseEntity.noContent().build();
    }
}

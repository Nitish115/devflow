package com.devflow.devflow.task;

import com.devflow.devflow.task.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/organizations/{orgId}/projects/{projectId}/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@PathVariable Long orgId,
                                                   @PathVariable Long projectId,
                                                   @Valid @RequestBody CreateTaskRequest request,
                                                   Principal principal) {
        TaskResponse response = taskService.createTask(orgId, projectId, request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getTasks(@PathVariable Long orgId,
                                                       @PathVariable Long projectId,
                                                       @RequestParam(required = false) TaskStatus status,
                                                       @RequestParam(required = false) Long assigneeId,
                                                       Principal principal) {
        List<TaskResponse> response = taskService.getTasks(orgId, projectId, status, assigneeId, principal.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long orgId,
                                                     @PathVariable Long projectId,
                                                     @PathVariable Long taskId,
                                                     Principal principal) {
        TaskResponse response = taskService.getTaskById(orgId, projectId, taskId, principal.getName());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable Long orgId,
                                                   @PathVariable Long projectId,
                                                   @PathVariable Long taskId,
                                                   @RequestBody UpdateTaskRequest request,
                                                   Principal principal) {
        TaskResponse response = taskService.updateTask(orgId, projectId, taskId, request, principal.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long orgId,
                                           @PathVariable Long projectId,
                                           @PathVariable Long taskId,
                                           Principal principal) {
        taskService.deleteTask(orgId, projectId, taskId, principal.getName());
        return ResponseEntity.noContent().build();
    }
}

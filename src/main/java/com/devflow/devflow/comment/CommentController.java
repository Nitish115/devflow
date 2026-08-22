package com.devflow.devflow.comment;

import com.devflow.devflow.comment.dto.CommentResponse;
import com.devflow.devflow.comment.dto.CreateCommentRequest;
import com.devflow.devflow.comment.dto.UpdateCommentRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/organizations/{orgId}/projects/{projectId}/tasks/{taskId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(@PathVariable Long orgId,
                                                         @PathVariable Long projectId,
                                                         @PathVariable Long taskId,
                                                         @Valid @RequestBody CreateCommentRequest request,
                                                         Principal principal) {
        CommentResponse response = commentService.createComment(orgId, projectId, taskId, request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long orgId,
                                                             @PathVariable Long projectId,
                                                             @PathVariable Long taskId,
                                                             Principal principal) {
        List<CommentResponse> response = commentService.getComments(orgId, projectId, taskId, principal.getName());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(@PathVariable Long orgId,
                                                         @PathVariable Long projectId,
                                                         @PathVariable Long taskId,
                                                         @PathVariable Long commentId,
                                                         @Valid @RequestBody UpdateCommentRequest request,
                                                         Principal principal) {
        CommentResponse response = commentService.updateComment(orgId, projectId, taskId, commentId, request, principal.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long orgId,
                                              @PathVariable Long projectId,
                                              @PathVariable Long taskId,
                                              @PathVariable Long commentId,
                                              Principal principal) {
        commentService.deleteComment(orgId, projectId, taskId, commentId, principal.getName());
        return ResponseEntity.noContent().build();
    }
}

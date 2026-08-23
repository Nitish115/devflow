package com.devflow.devflow.comment;

import com.devflow.devflow.auth.Role;
import com.devflow.devflow.auth.User;
import com.devflow.devflow.auth.UserRepository;
import com.devflow.devflow.comment.dto.CommentResponse;
import com.devflow.devflow.comment.dto.CreateCommentRequest;
import com.devflow.devflow.comment.dto.UpdateCommentRequest;
import com.devflow.devflow.common.exception.*;
import com.devflow.devflow.event.CommentCreatedEvent;
import com.devflow.devflow.event.CommentMentionedEvent;
import com.devflow.devflow.event.EventPublisher;
import com.devflow.devflow.organization.OrganizationMember;
import com.devflow.devflow.organization.OrganizationMemberRepository;
import com.devflow.devflow.project.ProjectMember;
import com.devflow.devflow.project.ProjectMemberRepository;
import com.devflow.devflow.project.ProjectService;
import com.devflow.devflow.task.Task;
import com.devflow.devflow.task.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private static final Pattern MENTION_PATTERN = Pattern.compile("@([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})");

    private final CommentRepository commentRepository;
    private final CommentMentionRepository commentMentionRepository;
    private final TaskRepository taskRepository;
    private final ProjectService projectService;
    private final ProjectMemberRepository projectMemberRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final UserRepository userRepository;
    private final EventPublisher eventPublisher;

    public CommentService(CommentRepository commentRepository,
                          CommentMentionRepository commentMentionRepository,
                          TaskRepository taskRepository,
                          ProjectService projectService,
                          ProjectMemberRepository projectMemberRepository,
                          OrganizationMemberRepository organizationMemberRepository,
                          UserRepository userRepository,
                          EventPublisher eventPublisher) {
        this.commentRepository = commentRepository;
        this.commentMentionRepository = commentMentionRepository;
        this.taskRepository = taskRepository;
        this.projectService = projectService;
        this.projectMemberRepository = projectMemberRepository;
        this.organizationMemberRepository = organizationMemberRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public CommentResponse createComment(Long orgId, Long projectId, Long taskId, CreateCommentRequest request, String userEmail) {
        User currentUser = getUserByEmail(userEmail);
        projectService.validateOrgMembership(orgId, currentUser.getId());
        projectService.getProjectEntity(orgId, projectId);
        projectService.validateProjectMembership(projectId, currentUser.getId());

        Task task = getTaskEntity(projectId, taskId);

        Comment comment = new Comment(task, currentUser, request.getContent());
        Comment savedComment = commentRepository.save(comment);

        List<Long> mentionedUserIds = processAndSaveMentions(savedComment, projectId, request.getContent());

        eventPublisher.publishCommentCreated(new CommentCreatedEvent(
                savedComment.getId(),
                taskId,
                projectId,
                orgId,
                currentUser.getId(),
                currentUser.getEmail(),
                savedComment.getContent()
        ));

        for (Long mentionedUserId : mentionedUserIds) {
            eventPublisher.publishCommentMentioned(new CommentMentionedEvent(
                    savedComment.getId(),
                    taskId,
                    projectId,
                    orgId,
                    currentUser.getId(),
                    currentUser.getEmail(),
                    mentionedUserId,
                    savedComment.getContent()
            ));
        }

        return mapToCommentResponse(savedComment, mentionedUserIds);
    }


    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Long orgId, Long projectId, Long taskId, String userEmail) {
        User currentUser = getUserByEmail(userEmail);
        projectService.validateOrgMembership(orgId, currentUser.getId());
        projectService.getProjectEntity(orgId, projectId);
        projectService.validateProjectMembership(projectId, currentUser.getId());

        getTaskEntity(projectId, taskId);

        List<Comment> comments = commentRepository.findAllByTaskIdOrderByCreatedAtAsc(taskId);
        return comments.stream()
                .map(comment -> {
                    List<Long> mentionedUserIds = commentMentionRepository.findAllByCommentId(comment.getId()).stream()
                            .map(mention -> mention.getUser().getId())
                            .collect(Collectors.toList());
                    return mapToCommentResponse(comment, mentionedUserIds);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentResponse updateComment(Long orgId, Long projectId, Long taskId, Long commentId, UpdateCommentRequest request, String userEmail) {
        User currentUser = getUserByEmail(userEmail);
        projectService.validateOrgMembership(orgId, currentUser.getId());
        projectService.getProjectEntity(orgId, projectId);
        projectService.validateProjectMembership(projectId, currentUser.getId());

        getTaskEntity(projectId, taskId);

        Comment comment = commentRepository.findByIdAndTaskId(commentId, taskId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found with id: " + commentId + " on task: " + taskId));

        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new InsufficientPermissionsException("Only the author can edit this comment");
        }

        comment.setContent(request.getContent());
        Comment updatedComment = commentRepository.save(comment);

        commentMentionRepository.deleteAllByCommentId(commentId);
        List<Long> mentionedUserIds = processAndSaveMentions(updatedComment, projectId, request.getContent());

        return mapToCommentResponse(updatedComment, mentionedUserIds);
    }

    @Transactional
    public void deleteComment(Long orgId, Long projectId, Long taskId, Long commentId, String userEmail) {
        User currentUser = getUserByEmail(userEmail);
        projectService.validateOrgMembership(orgId, currentUser.getId());
        projectService.getProjectEntity(orgId, projectId);
        projectService.validateProjectMembership(projectId, currentUser.getId());

        getTaskEntity(projectId, taskId);

        Comment comment = commentRepository.findByIdAndTaskId(commentId, taskId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found with id: " + commentId + " on task: " + taskId));

        boolean isAuthor = comment.getAuthor().getId().equals(currentUser.getId());
        boolean isElevated = canManageProjectOrOrg(orgId, projectId, currentUser.getId());

        if (!isAuthor && !isElevated) {
            throw new InsufficientPermissionsException("Only the comment author, Project Manager, or Organization Owner/Admin can delete this comment");
        }

        commentRepository.delete(comment);
    }

    private List<Long> processAndSaveMentions(Comment comment, Long projectId, String content) {
        if (content == null || content.isBlank()) {
            return Collections.emptyList();
        }

        Set<String> extractedEmails = parseMentionEmails(content);
        List<Long> mentionedUserIds = new ArrayList<>();

        for (String email : extractedEmails) {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (projectMemberRepository.existsByProjectIdAndUserId(projectId, user.getId())) {
                    CommentMention mention = new CommentMention(comment, user);
                    commentMentionRepository.save(mention);
                    mentionedUserIds.add(user.getId());
                }
            }
        }

        return mentionedUserIds;
    }

    private Set<String> parseMentionEmails(String content) {
        Set<String> emails = new LinkedHashSet<>();
        Matcher matcher = MENTION_PATTERN.matcher(content);
        while (matcher.find()) {
            emails.add(matcher.group(1));
        }
        return emails;
    }

    private Task getTaskEntity(Long projectId, Long taskId) {
        return taskRepository.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId + " in project: " + projectId));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    private boolean canManageProjectOrOrg(Long orgId, Long projectId, Long userId) {
        Optional<OrganizationMember> orgMember = organizationMemberRepository.findByOrganizationIdAndUserId(orgId, userId);
        if (orgMember.isPresent() && (orgMember.get().getRole() == Role.ORGANIZATION_OWNER || orgMember.get().getRole() == Role.ORGANIZATION_ADMIN)) {
            return true;
        }

        Optional<ProjectMember> projectMember = projectMemberRepository.findByProjectIdAndUserId(projectId, userId);
        return projectMember.isPresent() && projectMember.get().getRole() == Role.PROJECT_MANAGER;
    }

    private CommentResponse mapToCommentResponse(Comment comment, List<Long> mentionedUserIds) {
        return new CommentResponse(
                comment.getId(),
                comment.getTask().getId(),
                comment.getAuthor().getId(),
                comment.getAuthor().getEmail(),
                comment.getContent(),
                mentionedUserIds != null ? mentionedUserIds : Collections.emptyList(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}

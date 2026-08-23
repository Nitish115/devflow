package com.devflow.devflow.activity.listener;

import com.devflow.devflow.activity.ActivityLog;
import com.devflow.devflow.activity.ActivityLogRepository;
import com.devflow.devflow.auth.User;
import com.devflow.devflow.auth.UserRepository;
import com.devflow.devflow.config.RabbitMQConfig;
import com.devflow.devflow.event.CommentCreatedEvent;
import com.devflow.devflow.event.CommentMentionedEvent;
import com.devflow.devflow.event.TaskAssignedEvent;
import com.devflow.devflow.event.TaskStatusChangedEvent;
import com.devflow.devflow.organization.Organization;
import com.devflow.devflow.organization.OrganizationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RabbitListener(queues = RabbitMQConfig.ACTIVITY_LOG_QUEUE)
public class ActivityLogListener {

    private static final Logger log = LoggerFactory.getLogger(ActivityLogListener.class);

    private final ActivityLogRepository activityLogRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;

    public ActivityLogListener(ActivityLogRepository activityLogRepository,
                               OrganizationRepository organizationRepository,
                               UserRepository userRepository) {
        this.activityLogRepository = activityLogRepository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
    }

    @RabbitHandler
    public void handleTaskAssigned(TaskAssignedEvent event) {
        log.info("Received TaskAssignedEvent for activity log: taskId={}", event.getTaskId());
        try {
            Optional<Organization> orgOpt = organizationRepository.findById(event.getOrganizationId());
            Optional<User> actorOpt = userRepository.findById(event.getAssignedById());
            if (orgOpt.isPresent() && actorOpt.isPresent()) {
                String details = String.format("Assigned task '%s' (ID: %d) to user ID %d",
                        event.getTaskTitle(), event.getTaskId(), event.getAssigneeId());
                ActivityLog activityLog = new ActivityLog(orgOpt.get(), actorOpt.get(), "TASK_ASSIGNED", "TASK", event.getTaskId(), details);
                activityLogRepository.save(activityLog);
            }
        } catch (Exception e) {
            log.error("Failed to process TaskAssignedEvent for activity log: {}", e.getMessage(), e);
        }
    }

    @RabbitHandler
    public void handleTaskStatusChanged(TaskStatusChangedEvent event) {
        log.info("Received TaskStatusChangedEvent for activity log: taskId={}", event.getTaskId());
        try {
            Optional<Organization> orgOpt = organizationRepository.findById(event.getOrganizationId());
            Optional<User> actorOpt = userRepository.findById(event.getUpdatedById());
            if (orgOpt.isPresent() && actorOpt.isPresent()) {
                String details = String.format("Changed status of task '%s' (ID: %d) from %s to %s",
                        event.getTaskTitle(), event.getTaskId(), event.getOldStatus(), event.getNewStatus());
                ActivityLog activityLog = new ActivityLog(orgOpt.get(), actorOpt.get(), "TASK_STATUS_CHANGED", "TASK", event.getTaskId(), details);
                activityLogRepository.save(activityLog);
            }
        } catch (Exception e) {
            log.error("Failed to process TaskStatusChangedEvent for activity log: {}", e.getMessage(), e);
        }
    }

    @RabbitHandler
    public void handleCommentCreated(CommentCreatedEvent event) {
        log.info("Received CommentCreatedEvent for activity log: commentId={}", event.getCommentId());
        try {
            Optional<Organization> orgOpt = organizationRepository.findById(event.getOrganizationId());
            Optional<User> actorOpt = userRepository.findById(event.getAuthorId());
            if (orgOpt.isPresent() && actorOpt.isPresent()) {
                String details = String.format("Added comment (ID: %d) on task ID %d", event.getCommentId(), event.getTaskId());
                ActivityLog activityLog = new ActivityLog(orgOpt.get(), actorOpt.get(), "COMMENT_CREATED", "COMMENT", event.getCommentId(), details);
                activityLogRepository.save(activityLog);
            }
        } catch (Exception e) {
            log.error("Failed to process CommentCreatedEvent for activity log: {}", e.getMessage(), e);
        }
    }

    @RabbitHandler
    public void handleCommentMentioned(CommentMentionedEvent event) {
        log.info("Received CommentMentionedEvent for activity log: commentId={}", event.getCommentId());
        try {
            Optional<Organization> orgOpt = organizationRepository.findById(event.getOrganizationId());
            Optional<User> actorOpt = userRepository.findById(event.getAuthorId());
            if (orgOpt.isPresent() && actorOpt.isPresent()) {
                String details = String.format("Mentioned user ID %d in comment (ID: %d) on task ID %d",
                        event.getMentionedUserId(), event.getCommentId(), event.getTaskId());
                ActivityLog activityLog = new ActivityLog(orgOpt.get(), actorOpt.get(), "COMMENT_MENTIONED", "COMMENT", event.getCommentId(), details);
                activityLogRepository.save(activityLog);
            }
        } catch (Exception e) {
            log.error("Failed to process CommentMentionedEvent for activity log: {}", e.getMessage(), e);
        }
    }
}

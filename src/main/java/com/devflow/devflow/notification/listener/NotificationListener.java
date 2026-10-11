package com.devflow.devflow.notification.listener;

import com.devflow.devflow.auth.User;
import com.devflow.devflow.auth.UserRepository;
import com.devflow.devflow.config.RabbitMQConfig;
import com.devflow.devflow.event.CommentMentionedEvent;
import com.devflow.devflow.event.TaskAssignedEvent;
import com.devflow.devflow.event.TaskStatusChangedEvent;
import com.devflow.devflow.notification.NotificationService;
import com.devflow.devflow.notification.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
public class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public NotificationListener(NotificationService notificationService, UserRepository userRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @RabbitHandler
    public void handleTaskAssigned(TaskAssignedEvent event) {
        log.info("Received TaskAssignedEvent for notification: taskId={}, assigneeId={}", event.getTaskId(), event.getAssigneeId());
        try {
            if (event.getAssigneeId() == null) {
                return;
            }
            Optional<User> recipientOpt = userRepository.findById(event.getAssigneeId());
            if (recipientOpt.isPresent()) {
                String message = String.format("You have been assigned to task '%s' by %s",
                        event.getTaskTitle(), event.getAssignedByEmail());
                notificationService.createNotification(
                        recipientOpt.get(),
                        NotificationType.TASK_ASSIGNED,
                        "TASK",
                        event.getTaskId(),
                        message
                );
            } else {
                log.warn("Recipient user not found with id {} for TaskAssignedEvent", event.getAssigneeId());
            }
        } catch (Exception e) {
            log.error("Failed to process TaskAssignedEvent for notification: {}", e.getMessage(), e);
        }
    }

    @RabbitHandler
    public void handleTaskStatusChanged(TaskStatusChangedEvent event) {
        log.info("Received TaskStatusChangedEvent for notification: taskId={}, newStatus={}", event.getTaskId(), event.getNewStatus());
        try {
            String message = String.format("Task '%s' status changed from %s to %s by %s",
                    event.getTaskTitle(), event.getOldStatus(), event.getNewStatus(), event.getUpdatedByEmail());

            // Notify assignee if not the updater
            if (event.getAssigneeId() != null && !event.getAssigneeId().equals(event.getUpdatedById())) {
                userRepository.findById(event.getAssigneeId()).ifPresent(assignee ->
                        notificationService.createNotification(assignee, NotificationType.TASK_STATUS_CHANGED, "TASK", event.getTaskId(), message)
                );
            }

            // Notify creator if not the updater and not the assignee
            if (event.getTaskCreatorId() != null
                    && !event.getTaskCreatorId().equals(event.getUpdatedById())
                    && !event.getTaskCreatorId().equals(event.getAssigneeId())) {
                userRepository.findById(event.getTaskCreatorId()).ifPresent(creator ->
                        notificationService.createNotification(creator, NotificationType.TASK_STATUS_CHANGED, "TASK", event.getTaskId(), message)
                );
            }
        } catch (Exception e) {
            log.error("Failed to process TaskStatusChangedEvent for notification: {}", e.getMessage(), e);
        }
    }

    @RabbitHandler
    public void handleCommentMentioned(CommentMentionedEvent event) {
        log.info("Received CommentMentionedEvent for notification: commentId={}, mentionedUserId={}", event.getCommentId(), event.getMentionedUserId());
        try {
            if (event.getMentionedUserId() == null) {
                return;
            }
            Optional<User> recipientOpt = userRepository.findById(event.getMentionedUserId());
            if (recipientOpt.isPresent()) {
                String message = String.format("%s mentioned you in a comment on task #%d",
                        event.getAuthorEmail(), event.getTaskId());
                notificationService.createNotification(
                        recipientOpt.get(),
                        NotificationType.COMMENT_MENTION,
                        "COMMENT",
                        event.getCommentId(),
                        message
                );
            } else {
                log.warn("Mentioned user not found with id {} for CommentMentionedEvent", event.getMentionedUserId());
            }
        } catch (Exception e) {
            log.error("Failed to process CommentMentionedEvent for notification: {}", e.getMessage(), e);
        }
    }
}

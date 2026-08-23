package com.devflow.devflow.notification;

import com.devflow.devflow.auth.Role;
import com.devflow.devflow.auth.User;
import com.devflow.devflow.auth.UserRepository;
import com.devflow.devflow.event.CommentMentionedEvent;
import com.devflow.devflow.event.TaskAssignedEvent;
import com.devflow.devflow.event.TaskStatusChangedEvent;
import com.devflow.devflow.notification.listener.NotificationListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationListenerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationListener notificationListener;

    private User recipientUser;

    @BeforeEach
    void setUp() {
        recipientUser = new User(2L, "dev@example.com", "password", Role.DEVELOPER, null);
    }

    @Test
    void testHandleTaskAssigned() {
        TaskAssignedEvent event = new TaskAssignedEvent(100L, 10L, 1L, "Fix Bug", 2L, 1L, "owner@example.com");
        when(userRepository.findById(2L)).thenReturn(Optional.of(recipientUser));

        notificationListener.handleTaskAssigned(event);

        verify(notificationService).createNotification(
                eq(recipientUser),
                eq(NotificationType.TASK_ASSIGNED),
                eq("TASK"),
                eq(100L),
                contains("Fix Bug")
        );
    }

    @Test
    void testHandleTaskStatusChanged() {
        TaskStatusChangedEvent event = new TaskStatusChangedEvent(100L, 10L, 1L, "Fix Bug", "TODO", "IN_PROGRESS", 1L, "owner@example.com", 2L, 3L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(recipientUser));

        notificationListener.handleTaskStatusChanged(event);

        verify(notificationService).createNotification(
                eq(recipientUser),
                eq(NotificationType.TASK_STATUS_CHANGED),
                eq("TASK"),
                eq(100L),
                contains("IN_PROGRESS")
        );
    }

    @Test
    void testHandleCommentMentioned() {
        CommentMentionedEvent event = new CommentMentionedEvent(50L, 100L, 10L, 1L, 1L, "owner@example.com", 2L, "Hey @dev");
        when(userRepository.findById(2L)).thenReturn(Optional.of(recipientUser));

        notificationListener.handleCommentMentioned(event);

        verify(notificationService).createNotification(
                eq(recipientUser),
                eq(NotificationType.COMMENT_MENTION),
                eq("COMMENT"),
                eq(50L),
                contains("mentioned you")
        );
    }
}

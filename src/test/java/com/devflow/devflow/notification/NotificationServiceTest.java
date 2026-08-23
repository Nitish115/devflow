package com.devflow.devflow.notification;

import com.devflow.devflow.auth.Role;
import com.devflow.devflow.auth.User;
import com.devflow.devflow.auth.UserRepository;
import com.devflow.devflow.notification.dto.NotificationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User recipientUser;
    private Notification notification;

    @BeforeEach
    void setUp() {
        recipientUser = new User(1L, "user@example.com", "password", Role.DEVELOPER, null);
        notification = new Notification(10L, recipientUser, NotificationType.TASK_ASSIGNED, "TASK", 100L, "Assigned to task", false);
    }

    @Test
    void testGetUserNotifications_All() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Notification> page = new PageImpl<>(Collections.singletonList(notification));

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(recipientUser));
        when(notificationRepository.findAllByRecipientIdOrderByCreatedAtDesc(1L, pageable)).thenReturn(page);

        Page<NotificationResponse> responses = notificationService.getUserNotifications("user@example.com", false, 0, 20);

        assertEquals(1, responses.getTotalElements());
        assertEquals("Assigned to task", responses.getContent().get(0).getMessage());
    }

    @Test
    void testGetUserNotifications_UnreadOnly() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Notification> page = new PageImpl<>(Collections.singletonList(notification));

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(recipientUser));
        when(notificationRepository.findAllByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(1L, pageable)).thenReturn(page);

        Page<NotificationResponse> responses = notificationService.getUserNotifications("user@example.com", true, 0, 20);

        assertEquals(1, responses.getTotalElements());
        assertFalse(responses.getContent().get(0).isRead());
    }

    @Test
    void testMarkAsRead_Success() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(recipientUser));
        when(notificationRepository.findByIdAndRecipientId(10L, 1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        NotificationResponse response = notificationService.markAsRead(10L, "user@example.com");

        assertNotNull(response);
        assertTrue(notification.isRead());
        verify(notificationRepository).save(notification);
    }

    @Test
    void testMarkAllAsRead_Success() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(recipientUser));

        notificationService.markAllAsRead("user@example.com");

        verify(notificationRepository).markAllAsReadByRecipientId(1L);
    }
}

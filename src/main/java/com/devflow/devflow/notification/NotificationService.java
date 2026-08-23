package com.devflow.devflow.notification;

import com.devflow.devflow.auth.User;
import com.devflow.devflow.auth.UserRepository;
import com.devflow.devflow.common.exception.InvalidOperationException;
import com.devflow.devflow.common.exception.UserNotFoundException;
import com.devflow.devflow.notification.dto.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(String userEmail, boolean unreadOnly, int page, int size) {
        User user = getUserByEmail(userEmail);
        Pageable pageable = PageRequest.of(page, size);

        Page<Notification> notifications;
        if (unreadOnly) {
            notifications = notificationRepository.findAllByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(user.getId(), pageable);
        } else {
            notifications = notificationRepository.findAllByRecipientIdOrderByCreatedAtDesc(user.getId(), pageable);
        }

        return notifications.map(this::mapToNotificationResponse);
    }

    @Transactional
    public NotificationResponse markAsRead(Long id, String userEmail) {
        User user = getUserByEmail(userEmail);
        Notification notification = notificationRepository.findByIdAndRecipientId(id, user.getId())
                .orElseThrow(() -> new InvalidOperationException("Notification not found with id: " + id + " for current user"));

        notification.setRead(true);
        Notification updated = notificationRepository.save(notification);
        return mapToNotificationResponse(updated);
    }

    @Transactional
    public void markAllAsRead(String userEmail) {
        User user = getUserByEmail(userEmail);
        notificationRepository.markAllAsReadByRecipientId(user.getId());
    }

    @Transactional
    public Notification createNotification(User recipient, NotificationType type, String referenceType, Long referenceId, String message) {
        Notification notification = new Notification(recipient, type, referenceType, referenceId, message);
        return notificationRepository.save(notification);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    private NotificationResponse mapToNotificationResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getRecipient().getId(),
                notification.getType(),
                notification.getReferenceType(),
                notification.getReferenceId(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}

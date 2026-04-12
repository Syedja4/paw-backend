package com.pawnavz.service;

import com.pawnavz.dto.response.NotificationResponse;
import com.pawnavz.entity.Notification;
import com.pawnavz.entity.User;
import com.pawnavz.repository.NotificationRepository;
import com.pawnavz.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createNotification(String userId, Notification.NotificationType type,
            String title, String body, String referenceId, String referenceType) {
        User user = userRepository.getReferenceById(userId);
        notificationRepository.save(Notification.builder()
                .user(user).type(type).title(title).body(body)
                .referenceId(referenceId).referenceType(referenceType).build());
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(String userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAllAsRead(String userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    @Transactional
    public void markAsRead(String notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
    }

    private NotificationResponse mapToResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId()).type(n.getType().name()).title(n.getTitle())
                .body(n.getBody()).referenceId(n.getReferenceId())
                .referenceType(n.getReferenceType()).isRead(n.getIsRead())
                .createdAt(n.getCreatedAt()).build();
    }
}

package com.mb.reddit.service.implementation;

import com.mb.reddit.entity.CustomUserDetails;
import com.mb.reddit.entity.Notification;
import com.mb.reddit.repository.NotificationRepository;
import com.mb.reddit.service.NotificationService;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void addNotification(Notification notification) {
         notificationRepository.save(notification);
    }

    @Override
    public Integer getNotificationCount() {
        long getUserTimeStart = System.currentTimeMillis();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal() instanceof String) {
            return null;
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();
        long getUserTimeStop = System.currentTimeMillis();
        int notificationCount = notificationRepository.countUnreadNotificationsByUserId(userId);

        return notificationCount;
    }

    @Override
    public Page<Notification> getAllNotifications(int pageNumber, int pageSize, Long userId) {
        Sort sort = Sort.by("timestamp").descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<Notification> notifications = notificationRepository.getAllNotificationsByUser(userId, pageable);

        return notifications;
    }

    @Override
    @Transactional
    public void markAllAsReadForUser(Long userId) {
        notificationRepository.markAllAsReadForUser(userId);
    }

    @Override
    @Transactional
    public void deleteAllReadNotificationForUser(Long userId) {
        notificationRepository.deleteAllReadNotificationForUser(userId);
    }
}

package com.mb.reddit.service;

import com.mb.reddit.entity.Notification;
import org.springframework.data.domain.Page;

public interface NotificationService {

    void addNotification(Notification notification);
    Integer getNotificationCount();
    Page<Notification> getAllNotifications(int pageNumber, int pageSize, Long userId);
    void deleteAllReadNotificationForUser(Long userId);
}

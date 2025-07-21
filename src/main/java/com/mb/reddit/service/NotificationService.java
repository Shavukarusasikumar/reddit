package com.mb.reddit.service;

import com.mb.reddit.entity.Notification;
import com.mb.reddit.entity.User;
import org.springframework.data.domain.Page;

public interface NotificationService {

    void addNotification(Notification notification);

    int getNotificationCount();

    Page<Notification> getAllNotifications(int pageNumber, int pageSize, User currentUser);

    void markAllAsReadForUser(User currentUser);
}

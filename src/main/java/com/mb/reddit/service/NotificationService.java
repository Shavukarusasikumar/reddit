package com.mb.reddit.service;

import com.mb.reddit.entity.Notification;
import com.mb.reddit.entity.User;

public interface NotificationService {

    void addNotification(Notification notification);

    int getNotificationCount();
}

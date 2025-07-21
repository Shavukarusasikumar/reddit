package com.mb.reddit.service.implementation;

import com.mb.reddit.entity.Notification;
import com.mb.reddit.entity.User;
import com.mb.reddit.repository.NotificationRepository;
import com.mb.reddit.service.NotificationService;
import com.mb.reddit.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;

    public NotificationServiceImpl(NotificationRepository notificationRepository, UserService userService) {
        this.notificationRepository = notificationRepository;
        this.userService = userService;
    }

    @Override
    public void addNotification(Notification notification) {
         notificationRepository.save(notification);
    }

    @Override
    public int getNotificationCount() {
        User user = userService.getCurrentUser();
        if(user == null){
            return 0;
        }
        int notificationCount = notificationRepository.countUnreadNotifications(user);
        return notificationCount;
    }
}

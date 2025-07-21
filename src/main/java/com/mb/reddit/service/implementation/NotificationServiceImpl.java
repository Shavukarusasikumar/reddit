package com.mb.reddit.service.implementation;

import com.mb.reddit.entity.Notification;
import com.mb.reddit.entity.User;
import com.mb.reddit.repository.NotificationRepository;
import com.mb.reddit.service.NotificationService;
import com.mb.reddit.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
        User user = userService.getUserById(2L);
       // User user = userService.getCurrentUser(); TODO : uncomment this and make above commented
        if(user == null){
            return 0;
        }
        int notificationCount = notificationRepository.countUnreadNotifications(user);
        return notificationCount;
    }

    @Override
    public Page<Notification> getAllNotifications(int pageNumber, int pageSize, User currentUser) {
        Sort sort = Sort.by("timestamp").descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        return notificationRepository.getAllNotificationsByUser(currentUser, pageable);
    }

    @Override
    @Transactional
    public void markAllAsReadForUser(User currentUser) {
        notificationRepository.markAllAsReadForUser(currentUser);
    }
}

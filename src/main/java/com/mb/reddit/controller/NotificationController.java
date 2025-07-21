package com.mb.reddit.controller;

import com.mb.reddit.entity.Notification;
import com.mb.reddit.entity.User;
import com.mb.reddit.service.NotificationService;
import com.mb.reddit.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @GetMapping("notifications")
    public String getAllNotifications(@RequestParam(defaultValue = "0") int pageNumber,
                                      @RequestParam(defaultValue = "20") int pageSize,
                                      Model model){
        User currentUser = userService.getUserById(2L);


       // User currentUser = userService.getCurrentUser();
        if(currentUser == null){
            return "redirect:/login";
        }

        Page<Notification> notifications = notificationService.getAllNotifications(pageNumber, pageSize, currentUser);
        notificationService.markAllAsReadForUser(currentUser);

        model.addAttribute("notifications", notifications);
        model.addAttribute("totalPages", notifications.getTotalPages());
        return "notifications";
    }
}

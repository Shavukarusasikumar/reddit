package com.mb.reddit.controller;

import com.mb.reddit.entity.CustomUserDetails;
import com.mb.reddit.entity.Notification;
import com.mb.reddit.service.NotificationService;
import com.mb.reddit.service.UserService;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("notifications")
    public String getAllNotifications(@RequestParam(defaultValue = "0") int pageNumber,
                                      @RequestParam(defaultValue = "20") int pageSize,
                                      Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal() instanceof String) {
            return "redirect:/user/login";
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();

        Page<Notification> notifications = notificationService.getAllNotifications(pageNumber, pageSize, userId);

        notificationService.deleteAllReadNotificationForUser(userId);

        model.addAttribute("notifications", notifications);
        model.addAttribute("totalPages", notifications.getTotalPages());

        return "notifications";
    }
}

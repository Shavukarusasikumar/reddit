package com.mb.reddit.controller;

import com.mb.reddit.entity.*;
import com.mb.reddit.service.CommentService;
import com.mb.reddit.service.NotificationService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class CommentController {

    private final CommentService commentService;

    private final NotificationService notificationService;

    public CommentController(CommentService commentService, NotificationService notificationService) {
        this.commentService = commentService;
        this.notificationService = notificationService;
    }

    @PostMapping("/posts/{postId}/comments")
    @ResponseBody
    public String createComment(
            @PathVariable Long postId,
            @RequestParam String content,
            @RequestParam(required = false) Long parentCommentId,
            HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal() instanceof String) {
            return "redirect:/user/login";
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        Comment comment = new Comment();
        comment.setContent(content);

        Comment savedComment = commentService.createComment(comment, postId, parentCommentId);

        Post post = savedComment.getPost();

        if(!post.getAuthor().getUsername().equals(userDetails.getUsername())) {
            Notification notification = new Notification();
            notification.setRecipient(post.getAuthor());
            notification.setMessage(" commented on your post: "  + post.getTitle());
            notification.setType("COMMENT");
            notification.setRead(false);
            notification.setPost(post);
            notification.setSenderId(userDetails.getId());
            notification.setSenderName(userDetails.getName());
            notification.setTimestamp(LocalDateTime.now());

            notificationService.addNotification(notification);
        }

        String ajaxHeader = request.getHeader("X-Requested-With");

        if ("XMLHttpRequest".equals(ajaxHeader) ||
                "application/json".equals(request.getHeader("Accept")) ||
                request.getHeader("Content-Type") != null &&
                        request.getHeader("Content-Type").contains("application/x-www-form-urlencoded")) {
            return "success";
        }

        return "redirect:/posts/" + postId;
    }

    @GetMapping("/api/comments/{commentId}/vote-count")
    @ResponseBody
    public String getCommentVoteCount(@PathVariable Long commentId) {
        int voteCount = commentService.getVoteCountForComment(commentId);

        return String.valueOf(voteCount);
    }
}

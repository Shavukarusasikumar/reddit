package com.mb.reddit.controller;

import com.mb.reddit.entity.Comment;
import com.mb.reddit.entity.User;
import com.mb.reddit.service.CommentService;
import com.mb.reddit.service.implementation.UserServiceImpl;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class CommentController {

    private final CommentService commentService;
    private final UserServiceImpl userServiceImpl;

    public CommentController(CommentService commentService, UserServiceImpl userServiceImpl) {
        this.commentService = commentService;
        this.userServiceImpl = userServiceImpl;
    }

    public Comment getCommentById(long commentId) {
        return commentService.getCommentById(commentId);

    }

    @PostMapping("/delete-comment/{commentId}")
    public String deleteCommentById(@PathVariable Long commentId) {
        Long postId = commentService.getCommentById(commentId).getPost().getId();
        commentService.deleteCommentById(commentId);

        return "redirect:/posts/" + postId;
    }

    @GetMapping("/edit-comment/{commentId}")
    public String getCommentEditForm(@PathVariable Long commentId, Model model) {
        Comment comment = commentService.getCommentById(commentId);

        model.addAttribute("commentId", commentId);
        model.addAttribute("updatedContent", comment.getContent());

        return "edit-comment";
    }

    @PostMapping("/edit-comment/{commentId}")
    public String updateCommentById(
            @PathVariable Long commentId,
            @RequestParam("updatedContent") String updatedContent) {
        Long postId = commentService.getCommentById(commentId).getPost().getId();
        commentService.updateComment(commentId, updatedContent);

        return "redirect:/posts/" + postId;
    }

    @GetMapping("/get-all/{postId}")
    public String getCommentsByPostId(@PathVariable("postId") Long postId, Model model) {
        List<Comment> comments = commentService.getTopLevelComments(postId);

        model.addAttribute("comments", comments);

        return "comments";
    }

//    @PostMapping("/posts/{postId}/comments")
//    public String createComment(
//            @PathVariable Long postId,
//            @RequestParam String content,
//            @RequestParam(required = false) Long parentCommentId,
//            Authentication authentication) {
//        User user = userServiceImpl.getLoggedInUser();
//
//        if (user == null) {
//            return "redirect:/login";
//        }
//
//        Comment comment = new Comment();
//        comment.setContent(content);
//        commentService.createComment(comment, postId, parentCommentId);
//
//        return "redirect:/posts/" + postId;
//    }

    @PostMapping("/add-comment/{postId}/")
    public void createComment(@PathVariable Long postId, @RequestParam String content,
                              @RequestParam(required = false) Long parentCommentId, Authentication authentication) {

        if(authentication == null || !authentication.isAuthenticated()) {
            return;
        }

        Comment comment = new Comment();
        comment.setContent(content);
        commentService.createComment(comment, postId, parentCommentId);
    }
}

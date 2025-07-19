package com.mb.reddit.controller;

import com.mb.reddit.entity.Comment;
import com.mb.reddit.service.CommentService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
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

}

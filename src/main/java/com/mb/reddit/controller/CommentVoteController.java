package com.mb.reddit.controller;

import com.mb.reddit.service.CommentVoteService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/comments")
public class CommentVoteController {

    private final CommentVoteService commentVoteService;

    public CommentVoteController(CommentVoteService commentVoteService) {
        this.commentVoteService = commentVoteService;
    }

    @PostMapping("/{commentId}/upvote")
    public void upvoteComment(@PathVariable Long commentId, Authentication authentication) {
        if(authentication == null || !authentication.isAuthenticated()) {
            return;
        }

        Boolean currentVote = commentVoteService.getVoteStatusByCommentId(commentId);

        if(currentVote != null && currentVote) {
            commentVoteService.removeVoteByCommentId(commentId);
        }
        else {
            commentVoteService.addUpVoteByCommentId(commentId);
        }
    }

    @PostMapping("/{commentId}/downvote")
    public void downvoteComment(@PathVariable Long commentId, Authentication authentication) {
        if(authentication == null || !authentication.isAuthenticated()) {
            return;
        }

        Boolean currentVote = commentVoteService.getVoteStatusByCommentId(commentId);

        if(currentVote != null && !currentVote) {
            commentVoteService.removeVoteByCommentId(commentId);
        }
        else {
            commentVoteService.addDownVoteByCommentId(commentId);
        }
    }
}
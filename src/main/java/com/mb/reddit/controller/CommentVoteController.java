package com.mb.reddit.controller;

import com.mb.reddit.service.CommentVoteService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class CommentVoteController {

	private final CommentVoteService commentVoteService;

	public CommentVoteController(CommentVoteService commentVoteService) {
		this.commentVoteService = commentVoteService;
	}

	@PostMapping("/comments/{commentId}/upvote")
	public String upvoteComment(@PathVariable Long commentId, Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			return "redirect:/user/login?redirect=/comments/" + commentId;
		}

		Boolean currentVote = commentVoteService.getVoteStatusByCommentId(commentId);

		if (currentVote != null && currentVote) {
			commentVoteService.removeVoteByCommentId(commentId);
		} else {
			commentVoteService.addUpVoteByCommentId(commentId);
		}

		return "redirect:/posts/" + commentVoteService.getPostIdForComment(commentId);
	}

	@PostMapping("/comments/{commentId}/downvote")
	public String downvoteComment(@PathVariable Long commentId, Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			return "redirect:/user/login?redirect=/comments/" + commentId;
		}

		Boolean currentVote = commentVoteService.getVoteStatusByCommentId(commentId);

		if (currentVote != null && !currentVote) {
			commentVoteService.removeVoteByCommentId(commentId);
		} else {
			commentVoteService.addDownVoteByCommentId(commentId);
		}

		return "redirect:/posts/" + commentVoteService.getPostIdForComment(commentId);
	}
}
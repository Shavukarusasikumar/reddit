package com.mb.reddit.controller;

import com.mb.reddit.entity.CustomUserDetails;
import com.mb.reddit.entity.User;
import com.mb.reddit.service.CommentVoteService;
import com.mb.reddit.service.implementation.UserServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/comments")
public class CommentVoteController {

	private final CommentVoteService commentVoteService;
	private final UserServiceImpl userServiceImpl;

	public CommentVoteController(CommentVoteService commentVoteService, UserServiceImpl userServiceImpl) {
		this.commentVoteService = commentVoteService;
		this.userServiceImpl = userServiceImpl;
	}

	@PostMapping("/{commentId}/upvote")
	@ResponseBody
	public ResponseEntity<String> upvoteComment(@PathVariable Long commentId) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()
				|| authentication.getPrincipal() instanceof String) {
			return ResponseEntity.status(401).body("User not authenticated");
		}

		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
		Boolean currentVote = commentVoteService.getVoteStatusByCommentId(commentId);

		if (currentVote != null && currentVote) {
			commentVoteService.removeVoteByCommentId(commentId, userDetails.getId());
		} else {
			commentVoteService.addUpVoteByCommentId(commentId, userDetails.getId());
		}

		return ResponseEntity.ok("Vote updated successfully");
	}

@PostMapping("/{commentId}/downvote")
@ResponseBody
public ResponseEntity<String> downvoteComment(@PathVariable Long commentId) {
	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

	if (authentication == null || !authentication.isAuthenticated()
			|| authentication.getPrincipal() instanceof String) {
		return ResponseEntity.status(401).body("User not authenticated");
	}

	CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
	Boolean currentVote = commentVoteService.getVoteStatusByCommentId(commentId);

	if (currentVote != null && !currentVote) {
		commentVoteService.removeVoteByCommentId(commentId, userDetails.getId());
	} else {
		commentVoteService.addDownVoteByCommentId(commentId, userDetails.getId());
	}

	return ResponseEntity.ok("Vote updated successfully");
}

	@PostMapping("/{commentId}/remove-vote")
	@ResponseBody
	public ResponseEntity<String> removeVote(@PathVariable Long commentId) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()
				|| authentication.getPrincipal() instanceof String) {
			return ResponseEntity.status(401).body("User not authenticated");
		}

		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
		commentVoteService.removeVoteByCommentId(commentId, userDetails.getId());

		return ResponseEntity.ok("Vote removed successfully");
	}
}
package com.mb.reddit.controller;

import com.mb.reddit.entity.User;
import com.mb.reddit.service.CommentVoteService;
import com.mb.reddit.service.implementation.UserServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

//	@PostMapping("/{commentId}/upvote")
//	public void upvoteComment(@PathVariable Long commentId, Authentication authentication) {
//		if(authentication == null || !authentication.isAuthenticated()) {
//			return;
//		}
//
//		User user = userServiceImpl.getLoggedInUser();
//
//		Boolean currentVote = commentVoteService.getVoteStatusByCommentId(commentId);
//
//		if(currentVote != null && currentVote) {
//			commentVoteService.removeVoteByCommentId(commentId, user);
//		}
//		else {
//			commentVoteService.addUpVoteByCommentId(commentId,  user);
//		}
//	}

	@PostMapping("/{commentId}/upvote")
	@ResponseBody
	public ResponseEntity<String> upvoteComment(@PathVariable Long commentId) {
		User user = userServiceImpl.getLoggedInUser();

		if (user == null) {
			return ResponseEntity.status(401).body("User not authenticated");
		}

		Boolean currentVote = commentVoteService.getVoteStatusByCommentId(commentId);

		if (currentVote != null && currentVote) {
			commentVoteService.removeVoteByCommentId(commentId, user);
		} else {
			commentVoteService.addUpVoteByCommentId(commentId, user);
		}

		return ResponseEntity.ok("Vote updated successfully");
	}
//	@PostMapping("/{commentId}/downvote")
//	public void downvoteComment(@PathVariable Long commentId, Authentication authentication) {
//		User user = userServiceImpl.getLoggedInUser();
////
////		if (user == null) {
//////			return "redirect:/user/login";
////		}
//
//		Boolean currentVote = commentVoteService.getVoteStatusByCommentId(commentId);
//
//		if(currentVote != null && !currentVote) {
//			commentVoteService.removeVoteByCommentId(commentId,  user);
//		}
//		else {
//			commentVoteService.addDownVoteByCommentId(commentId,   user);
//		}
//	}

@PostMapping("/{commentId}/downvote")
@ResponseBody
public ResponseEntity<String> downvoteComment(@PathVariable Long commentId) {
	User user = userServiceImpl.getLoggedInUser();

	if (user == null) {
		return ResponseEntity.status(401).body("User not authenticated");
	}

	Boolean currentVote = commentVoteService.getVoteStatusByCommentId(commentId);

	if (currentVote != null && !currentVote) {
		commentVoteService.removeVoteByCommentId(commentId, user);
	} else {
		commentVoteService.addDownVoteByCommentId(commentId, user);
	}

	return ResponseEntity.ok("Vote updated successfully");
}
}
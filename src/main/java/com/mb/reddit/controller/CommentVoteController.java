package com.mb.reddit.controller;

import com.mb.reddit.entity.User;
import com.mb.reddit.service.CommentVoteService;
import com.mb.reddit.service.implementation.UserServiceImpl;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
	public void upvoteComment(@PathVariable Long commentId, Authentication authentication) {
		if(authentication == null || !authentication.isAuthenticated()) {
			return;
		}

		User user = userServiceImpl.getLoggedInUser();

		Boolean currentVote = commentVoteService.getVoteStatusByCommentId(commentId);

		if(currentVote != null && currentVote) {
			commentVoteService.removeVoteByCommentId(commentId, user);
		}
		else {
			commentVoteService.addUpVoteByCommentId(commentId,  user);
		}
	}
//	@PostMapping("/comments/{commentId}/upvote")
//	public String upvoteComment(@PathVariable Long commentId) {
//		User user = userServiceImpl.getLoggedInUser();
//
//		if (user == null) {
//			return "redirect:/user/login";
//		}
//
//		Boolean currentVote = commentVoteService.getVoteStatusByCommentId(commentId);
//
//		if (currentVote != null && currentVote) {
//			commentVoteService.removeVoteByCommentId(commentId, user);
//		} else {
//			commentVoteService.addUpVoteByCommentId(commentId, user);
//		}
//
//		return "redirect:/posts/" + commentVoteService.getPostIdForComment(commentId);
//	}

	@PostMapping("/{commentId}/downvote")
	public void downvoteComment(@PathVariable Long commentId, Authentication authentication) {
		User user = userServiceImpl.getLoggedInUser();
//
//		if (user == null) {
////			return "redirect:/user/login";
//		}

		Boolean currentVote = commentVoteService.getVoteStatusByCommentId(commentId);

		if(currentVote != null && !currentVote) {
			commentVoteService.removeVoteByCommentId(commentId,  user);
		}
		else {
			commentVoteService.addDownVoteByCommentId(commentId,   user);
		}
	}

//@PostMapping("/comments/{commentId}/downvote")
//public String downvoteComment(@PathVariable Long commentId) {
//	User user = userServiceImpl.getLoggedInUser();
//
//	if (user == null) {
//		return "redirect:/user/login";
//	}
//
//	Boolean currentVote = commentVoteService.getVoteStatusByCommentId(commentId);
//
//	if (currentVote != null && !currentVote) {
//		commentVoteService.removeVoteByCommentId(commentId, user);
//	} else {
//		commentVoteService.addDownVoteByCommentId(commentId, user);
//	}
//
//	return "redirect:/posts/" + commentVoteService.getPostIdForComment(commentId);
//}
}
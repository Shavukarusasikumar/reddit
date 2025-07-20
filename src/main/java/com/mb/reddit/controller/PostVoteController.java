package com.mb.reddit.controller;

import com.mb.reddit.service.PostVoteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class PostVoteController {

	private final PostVoteService postVoteService;

	public PostVoteController(PostVoteService postVoteService) {
		this.postVoteService = postVoteService;
	}

	@PostMapping("/posts/{postId}/upvote")
	public String upvotePost(@PathVariable Long postId) {
		postVoteService.addVoteByPostId(postId, true);
		return "redirect:/posts/" + postId;
	}

	@PostMapping("/posts/{postId}/downvote")
	public String downvotePost(@PathVariable Long postId) {
		postVoteService.addVoteByPostId(postId, false);
		return "redirect:/posts/" + postId;
	}
}
package com.mb.reddit.controller;

import com.mb.reddit.service.PostVoteService;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/votes")
public class PostVoteController {
    private final PostVoteService postVoteService;

    public PostVoteController(PostVoteService postVoteService) {
        this.postVoteService = postVoteService;
    }

    @PostMapping("/upvote/{postId}")
    @ResponseBody
    public ResponseEntity<String> upvotePost(@PathVariable Long postId) {
        try {
            postVoteService.addVoteByPostId(postId, true);

            return ResponseEntity.ok("Upvoted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to upvote");
        }
    }

    @PostMapping("/downvote/{postId}")
    @ResponseBody
    public ResponseEntity<String> downvotePost(@PathVariable Long postId) {
        try {
            postVoteService.addVoteByPostId(postId, false);

            return ResponseEntity.ok("Downvoted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to downvote");
        }
    }

    @DeleteMapping("/remove/{postId}")
    @ResponseBody
    public ResponseEntity<String> removeVote(@PathVariable Long postId) {
        try {
            postVoteService.removeVoteByPostId(postId);

            return ResponseEntity.ok("Vote removed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to remove vote");
        }
    }
}
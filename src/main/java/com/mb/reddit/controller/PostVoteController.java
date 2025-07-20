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
    public ResponseEntity<String> upvote(@PathVariable Long postId) {
        postVoteService.addVoteByPostId(postId, true);

        return ResponseEntity.ok("Upvoted");
    }

    @PostMapping("/downvote/{postId}")
    @ResponseBody
    public ResponseEntity<String> downvote(@PathVariable Long postId) {
        postVoteService.addVoteByPostId(postId, false);

        return ResponseEntity.ok("Downvoted");
    }

    @DeleteMapping("/remove/{postId}")
    @ResponseBody
    public ResponseEntity<String> removeVote(@PathVariable Long postId) {
        postVoteService.removeVoteByPostId(postId);

        return ResponseEntity.ok("Vote removed");
    }
}

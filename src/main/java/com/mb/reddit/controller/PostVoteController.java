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
    public void upvote(@PathVariable Long postId) {
        try {
            postVoteService.addVoteByPostId(postId, true);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/downvote/{postId}")
    @ResponseBody
    public void downvote(@PathVariable Long postId) {
        try {
            postVoteService.addVoteByPostId(postId, false);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @DeleteMapping("/remove/{postId}")
    @ResponseBody
    public void removeVote(@PathVariable Long postId) {
        try {
            postVoteService.removeVoteByPostId(postId);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
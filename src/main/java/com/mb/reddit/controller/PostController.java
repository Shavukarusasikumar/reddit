package com.mb.reddit.controller;

import com.mb.reddit.service.PostService;
import org.springframework.stereotype.Controller;

@Controller
public class PostController {

    public final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

}

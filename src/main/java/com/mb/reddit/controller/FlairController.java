package com.mb.reddit.controller;

import com.mb.reddit.service.FlairService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class FlairController {

    private final FlairService flairService;

    public FlairController(FlairService flairService) {
        this.flairService = flairService;
    }

    @GetMapping
    public void getFlairById(@PathVariable long flairId) {
        flairService.getFlairById(flairId);
    }

    @GetMapping
    public void getAllFlairsByCommunityId(@PathVariable long communityId) {
        flairService.getAllFlairsByCommunityId(communityId);
    }
}

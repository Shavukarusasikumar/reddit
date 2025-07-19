package com.mb.reddit.controller;

import com.mb.reddit.entity.Flair;
import com.mb.reddit.service.FlairService;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class FlairController {

    private final FlairService flairService;

    public FlairController(FlairService flairService) {
        this.flairService = flairService;
    }

    @PostMapping
    public void createFlair(@PathVariable long communityId, @ModelAttribute Flair flair) {
        flairService.createFlair(communityId, flair);
    }

    @GetMapping
    public void getFlairById(@PathVariable long flairId) {
        flairService.getFlairById(flairId);
    }

    @GetMapping
    public void getAllFlairsByCommunityId(@PathVariable long communityId) {
        flairService.getAllFlairsByCommunityId(communityId);
    }

    @PostMapping
    public void deleteFlairById(long flairId) {
        flairService.deleteFlairById(flairId);
    }
}

package com.mb.reddit.controller;

import com.mb.reddit.entity.Community;
import com.mb.reddit.entity.Topic;
import com.mb.reddit.service.CommunityService;
import com.mb.reddit.service.TopicService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Controller
public class CommunityController {

    private final CommunityService communityService;
    private final TopicService topicService;

    public CommunityController(CommunityService communityService , TopicService topicService) {
        this.communityService = communityService;
        this.topicService = topicService;
    }


    @GetMapping("/create-community")
    public String showCreateCommunityForm(Model model) {
        List<Topic> allTopics = topicService.getAllTopics();

        model.addAttribute("allTopics", allTopics);
        return "create-community";
    }

    @PostMapping("/create-community")
    public String handleCreateCommunity(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam(value = "icon", required = false) MultipartFile iconFile,
            @RequestParam(value = "banner", required = false) MultipartFile bannerFile,
            @RequestParam("topics") String topicIds,
            @RequestParam String privacy) {

        List<Long> topics = Arrays.stream(topicIds.split(","))
                .map(Long::parseLong)
                .toList();

        if (topics.size() > 3) {
            throw new IllegalArgumentException("You can select up to 3 topics only.");
        }

        Community community = new Community();
        community.setName(name);
        community.setDescription(description);
        community.setIsPrivate((privacy.equals("private")));

        community = communityService.createCommunity(community, iconFile, bannerFile);

        List<Topic> selectedTopics = topicService.getAllTopicsByIds(topics);
        for (Topic topic : selectedTopics) {
            topic.setCommunity(community);
        }
        return "redirect:/";
    }
}

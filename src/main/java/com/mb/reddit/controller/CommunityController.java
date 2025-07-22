package com.mb.reddit.controller;

import com.mb.reddit.entity.Community;
import com.mb.reddit.entity.Topic;
import com.mb.reddit.entity.User;
import com.mb.reddit.service.CommunityService;
import com.mb.reddit.service.TopicService;

import com.mb.reddit.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@Controller
public class CommunityController {

    private final CommunityService communityService;
    private final TopicService topicService;
    private final UserService userService;

    public CommunityController(CommunityService communityService , TopicService topicService,
                               UserService userService) {
        this.communityService = communityService;
        this.topicService = topicService;
        this.userService = userService;
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
        community.setCreatedAt(LocalDateTime.now());
        community.setCreator(userService.getCurrentUser());

        community = communityService.createCommunity(community, iconFile, bannerFile);

        List<Topic> selectedTopics = topicService.getAllTopicsByIds(topics);
        for (Topic topic : selectedTopics) {
            topic.setCommunity(community);
        }
        return "redirect:/";
    }

    @GetMapping("/community/r/{communityId}")
    public String getCommunityView(@PathVariable Long communityId, Model model) {
        User currentUser = userService.getCurrentUser();

        Community community = communityService.getCommunityById(communityId);
        model.addAttribute("community", community);

        boolean isCreator = currentUser != null &&
                community.getCreator().getId().equals(currentUser.getId());
        model.addAttribute("isCreator", isCreator);

        boolean hasJoined = currentUser != null &&
                community.getMembers().contains(currentUser);
        model.addAttribute("hasJoined", hasJoined);

        return "community-profile";
    }

    @GetMapping("/r/{communityName}")
    public String getCommunity(@PathVariable String communityName, Model model,
                                   @AuthenticationPrincipal User currentUser) {
        Community community = communityService.getCommunityByName(communityName);
        model.addAttribute("community", community);

        boolean isCreator = currentUser != null &&
                community.getCreator().getId().equals(currentUser.getId());
        model.addAttribute("isCreator", isCreator);

        boolean hasJoined = currentUser != null &&
                community.getMembers().contains(currentUser);
        model.addAttribute("hasJoined", hasJoined);

        return "community-profile";
    }
}

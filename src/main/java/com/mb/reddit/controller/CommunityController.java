package com.mb.reddit.controller;

import com.mb.reddit.entity.Community;
import com.mb.reddit.entity.Post;
import com.mb.reddit.entity.Topic;
import com.mb.reddit.entity.User;
import com.mb.reddit.service.*;

import com.mb.reddit.service.implementation.UserServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
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
    private final PostService postService;
    private final UserServiceImpl userServiceImpl;
    private final JoinRequestService joinRequestService;

    public CommunityController(CommunityService communityService, TopicService topicService, UserService userService, PostService postService, UserServiceImpl userServiceImpl, JoinRequestService joinRequestService) {
        this.communityService = communityService;
        this.topicService = topicService;
        this.userService = userService;
        this.postService = postService;
        this.userServiceImpl = userServiceImpl;
        this.joinRequestService = joinRequestService;
    }


    @GetMapping("/create-community")
    public String showCreateCommunityForm(Model model) {
        List<Topic> allTopics = topicService.getAllTopics();

        model.addAttribute("allTopics", allTopics);
        model.addAttribute("notificationCount",0);

        return "create-community";
    }

    @PostMapping("/create-community")
    public String handleCreateCommunity(@RequestParam String name, @RequestParam String description, @RequestParam(value = "icon", required = false) MultipartFile iconFile, @RequestParam(value = "banner", required = false) MultipartFile bannerFile, @RequestParam("topics") String topicIds, @RequestParam String privacy) {

        List<Long> topics = Arrays.stream(topicIds.split(",")).map(Long::parseLong).toList();

        if(topics.size() > 3) {
            throw new IllegalArgumentException("You can select up to 3 topics only.");
        }

        Community community = new Community();
        community.setName(name);
        community.setDescription(description);
        community.setIsPrivate((privacy.equals("private")));
        community.setCreatedAt(LocalDateTime.now());
        community.setCreator(userService.getCurrentUser());

        List<Topic> selectedTopics = topicService.getAllTopicsByIds(topics);
        community.setTopics(selectedTopics);

        communityService.createCommunity(community, iconFile, bannerFile);

        return "redirect:/";
    }

    @GetMapping("/community/r/{communityId}")
    public String getCommunityView(@PathVariable Long communityId, Model model) {
        User currentUser = userService.getCurrentUser();

        Community community = communityService.getCommunityById(communityId);
        model.addAttribute("community", community);

        boolean isCreator = currentUser != null && community.getCreator().getId().equals(currentUser.getId());
        model.addAttribute("isCreator", isCreator);

        boolean hasJoined = currentUser != null && community.getMembers().contains(currentUser);
        model.addAttribute("hasJoined", hasJoined);

        return "community-profile";
    }

    @GetMapping("/r/{communityName}")
    public String getCommunity(@PathVariable String communityName, Model model,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size) {
        Community community = communityService.getCommunityByName(communityName);

        User currentUser =  userService.getCurrentUser();
        boolean isCreator = currentUser != null && community.getCreator().getId().equals(currentUser.getId());
        boolean hasJoined = currentUser != null && community.getMembers().contains(currentUser);

        boolean isAdmin = community.getCreator().equals(currentUser);
        boolean isMember = currentUser.getJoinedCommunities().contains(community);

        boolean showContent = !(community.getIsPrivate() && !isMember);

        Page<Post> postsPage = postService.getPostsByCommunityId(community.getId(), page, size);

        model.addAllAttributes(Map.of("community", community, "isCreator", isCreator, "hasJoined", hasJoined, "posts", postsPage.getContent(), "currentPage", postsPage.getNumber(), "totalPages", postsPage.getTotalPages(), "size", size));
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isMember",isMember);
        model.addAttribute("showContent", showContent);

        return "community-profile";
    }

    @PostMapping("/user/join-community/{communityId}")
    public String joinCommunity(@PathVariable Long communityId) {
        User currentUser = userService.getCurrentUser();
        if (currentUser != null) {
            System.out.println(currentUser.getId());
            communityService.addMemberByCommunityId(currentUser, communityId);

        } else{
            System.out.println(currentUser.getId());

        }

        return "redirect:/r/" + communityService.getCommunityById(communityId).getName();
    }

    @PostMapping("/user/leave-community/{communityId}")
    public String leaveCommunity(@PathVariable Long communityId) {
        User currentUser = userService.getCurrentUser();
        if (currentUser != null) {
            communityService.removeMemberByCommunityId(currentUser, communityId);
        }

        return "redirect:/r/" + communityService.getCommunityById(communityId).getName();
    }


    @PostMapping("/api/communities/{id}/join")
    @ResponseBody
    public ResponseEntity<?> joinNewCommunity(@PathVariable Long id){
        System.out.println("WAS HERE NC");
        User currentUser = userService.getCurrentUser();
        if (currentUser == null){
            return ResponseEntity.badRequest().build();
        } else{
            communityService.addMemberByCommunityId(currentUser, id);

            return ResponseEntity.ok().build();
        }
    }

    @PostMapping("/api/communities/{id}/request")
    @ResponseBody
    public ResponseEntity<?> requestToJoinCommunity(@PathVariable Long id) {
        User user = userService.getCurrentUser();

        if (user == null){
            return ResponseEntity.badRequest().build();
        } else {
            Community community = communityService.getCommunityById(id);

            joinRequestService.sendJoinRequest(community, user);
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/communities/{id}/leave")
    public ResponseEntity<?> leaveCommunityRB(@PathVariable Long id) {
        User currentUser = userService.getCurrentUser();

        if (currentUser != null) {
            communityService.removeMemberByCommunityId(currentUser, id);

            return ResponseEntity.ok().build();
        } else{
            return ResponseEntity.badRequest().build();
        }
    }
}

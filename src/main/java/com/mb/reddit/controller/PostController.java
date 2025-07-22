package com.mb.reddit.controller;

import com.mb.reddit.dto.PostWithVotesDTO;
import com.mb.reddit.entity.*;
import com.mb.reddit.service.*;
import com.mb.reddit.entity.User;
import com.mb.reddit.service.CommunityService;
import com.mb.reddit.service.FlairService;
import com.mb.reddit.service.PostService;

import com.mb.reddit.service.PostVoteService;
import com.mb.reddit.service.implementation.UserServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.Authentication;
import com.mb.reddit.service.UserService;
import org.springframework.ui.Model;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Controller
public class PostController {

    private final PostService postService;
    private final CommentService commentService;
    private final PostVoteService postVoteService;
    private final UserService userService;
    private final CommunityService communityService;
    public final FlairService flairService;
    public final NotificationService notificationService;
    public final UserServiceImpl userServiceImpl;

    public PostController(PostService postService, UserService userService,
                          CommunityService communityService, FlairService flairService,
                          CommentService commentService, PostVoteService postVoteService,
                          NotificationService notificationService, UserServiceImpl userServiceImpl) {
        this.postService = postService;
        this.commentService = commentService;
        this.postVoteService = postVoteService;
        this.userService = userService;
        this.communityService = communityService;
        this.flairService = flairService;
        this.userServiceImpl = userServiceImpl;
        this.notificationService = notificationService;
    }

    @GetMapping("/new-post")
    public String getCreatePostForm(@RequestParam(name = "c", required = false) Long communityId, Model model) {
        List<Community> communities = communityService.findCommunitiesUserCanPost();
        model.addAttribute("communities", communities);

        Community selectedCommunity = null;
        List<Flair> flairs = List.of();

        if(communityId != null) {
            selectedCommunity = communityService.getCommunityById(communityId);
            flairs = flairService.getAllFlairsByCommunityId(communityId);
        }

        model.addAttribute("notificationCount", 0);
        model.addAttribute("selectedCommunity", selectedCommunity);
        model.addAttribute("flairs", flairs);

        model.addAttribute("postForm", new Post());

        return "create-post";
    }

    @PostMapping("/new-post")
    public String createPost(@ModelAttribute("post") Post post, @RequestParam Long communityId,
                             @RequestParam(value = "file", required = false) MultipartFile file) {
        postService.createPost(post, communityId, file);

        return "redirect:/home";
    }

    @GetMapping("/posts")
    public String getAllPosts(@RequestParam(defaultValue = "0", required = false) int pageNumber, @RequestParam(defaultValue = "10", required = false) int pageSize, @RequestParam(defaultValue = "createdAt", required = false) String sortBy, @RequestParam(defaultValue = "false", required = false) boolean rising, @RequestParam(defaultValue = "false", required = false) boolean top, @RequestParam(defaultValue = "false") boolean isNew, @RequestParam(defaultValue = "false") boolean popular, Model model) {
        long start = System.currentTimeMillis();
        Page<PostWithVotesDTO> posts = postService.getAllPost(pageNumber, pageSize, sortBy, rising, top, isNew, popular);

        long dbTime = System.currentTimeMillis();
        System.out.println("Controller : DB fetch time: " + (dbTime - start) + " ms");

        long cumminityStart = System.currentTimeMillis();
        List<Community> joinedCommunities = communityService.findUserJoinedCommunities();
        long communityEnd = System.currentTimeMillis();
        System.out.println("Community fetching time : " + (communityEnd - cumminityStart) + " ms");

        List<Community> recentCommunities = joinedCommunities.stream().limit(5).toList();

        List<PostWithVotesDTO> latest10Posts = posts.getContent();
        long startNotification = System.currentTimeMillis();
            Integer notificationCount = notificationService.getNotificationCount();
        long stopNotification = System.currentTimeMillis();
            System.out.println("notification count time: " + (stopNotification - startNotification));

        model.addAttribute("notificationCount", notificationCount);

        model.addAttribute("posts", posts.getContent());
        model.addAttribute("recentPosts", latest10Posts);
        model.addAttribute("communities", joinedCommunities);
        model.addAttribute("recentCommunities", recentCommunities);
        model.addAttribute("hasNext", posts.hasNext());
        model.addAttribute("isRising", rising);
        model.addAttribute("isTop", top);
        model.addAttribute("isNew", isNew);
        model.addAttribute("popular", popular);

        return "home";
    }

    @GetMapping("/posts/scroll")
    public String getMorePosts(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "false", required = false) boolean rising,
            @RequestParam(defaultValue = "false", required = false) boolean top,
            @RequestParam(defaultValue = "false") boolean isNew,
            @RequestParam(defaultValue = "false") boolean popular,
            Model model) {

        Page<PostWithVotesDTO> posts = postService.getAllPost(pageNumber, pageSize, sortBy, rising, top, isNew, popular);

        model.addAttribute("posts", posts.getContent());
        model.addAttribute("hasNext", posts.hasNext());
        model.addAttribute("isRising", rising);
        model.addAttribute("isTop", top);
        model.addAttribute("isNew", isNew);
        model.addAttribute("popular", popular);

        // return the lightweight fragment
        return "home";
    }

    @GetMapping("/posts/{postId}")
    public String getPostById(@PathVariable Long postId, Model model, Authentication authentication) {
        Post post = postService.getPostById(postId);
        List<Comment> topLevelComments = commentService.getTopLevelComments(postId);
        int postVoteCount = postService.getPostVotesByPostId(postId);
        int commentCount = topLevelComments.size();

        Boolean currentUserVote = null;
        if (authentication != null && authentication.isAuthenticated()) {
            currentUserVote = postVoteService.getVoteStatusByPostId(postId);
        }
        Community community = post.getCommunity();
        User owner = community.getCreator();

        Map<Long, Integer> commentVotes = new HashMap<>();
        for(Comment comment : topLevelComments) {
            int voteCount = commentService.getVoteCountForComment(comment.getId());
            commentVotes.put(comment.getId(), voteCount);
        }

        boolean isAuthenticated = authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String);

        model.addAttribute("isAuthenticated", isAuthenticated);
        boolean isJoined = false;
        if (isAuthenticated) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            if (owner.getUsername().equals(userDetails.getUsername())) {
                isJoined = userService.hasUserJoinedCommunity(community);
            }
        }
        boolean isSaved = false;
        if (isAuthenticated) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            if (userDetails != null) {
                isSaved = userService.isPostSavedByUser(postId, userDetails.getId());
            }
        }
        model.addAttribute("isSaved", isSaved);
        model.addAttribute("community", community);
        model.addAttribute("owner", owner);
        model.addAttribute("isJoined", isJoined);
        model.addAttribute("post", post);
        model.addAttribute("topLevelComments", topLevelComments);
        model.addAttribute("commentVotes", commentVotes);
        model.addAttribute("postVoteCount", postVoteCount);
        model.addAttribute("commentCount", commentCount);
        model.addAttribute("currentUserVote", currentUserVote);

        return "view-post";
    }

    @PostMapping("/delete/post/{postId}")
    public String deletePostById(@PathVariable("postId") Long postId) {
        postService.deletePost(postId);

        return "redirect:/";
    }

    @GetMapping("/user/{userId}/posts")
    public String getUserPosts(@PathVariable Long userId,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               Model model) {
        Page<Post> posts = postService.getPostsByUserId(userId, page, size);
        model.addAttribute("posts", posts);
        model.addAttribute("hasNext", posts.hasNext());
        return "fragments/posts :: postSection";
    }

    @GetMapping("/user/{userId}/upvoted")
    public String getUserUpVotedPosts(@PathVariable Long userId,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               Model model) {
        Page<Post> posts = postService.getUpvotedPostsByUserId(userId, page, size);
        model.addAttribute("posts", posts.getContent());
        model.addAttribute("hasNext", posts.hasNext());
        return "fragments/posts :: postSection";
    }

    @GetMapping("/user/{userId}/downvoted")
    public String getUserDownVotedPosts(@PathVariable Long userId,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size,
                                      Model model) {
        Page<Post> posts = postService.getDownVotedPostsByUserId(userId, page, size);
        model.addAttribute("posts", posts);
        model.addAttribute("hasNext", posts.hasNext());
        return "fragments/posts :: postSection";
    }

    @GetMapping("/user/{userId}/saved")
    public String getUserSavedPosts(@PathVariable Long userId,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size,
                                    Model model) {
        Page<Post> posts = postService.getSavedPostsByUserId(userId, page, size);
        model.addAttribute("posts", posts.getContent());
        model.addAttribute("hasNext", posts.hasNext());
        return "fragments/posts :: postSection";
    }

    @PostMapping("/save/{postId}")
    @ResponseBody
    public ResponseEntity<String> savePost(@PathVariable("postId") Long postId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal() instanceof String) {
            return ResponseEntity.status(401).body("Authentication required");
        }

        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            userService.addPostToUserSavedPosts(postId, userDetails.getId());
            return ResponseEntity.ok("Post saved successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error saving post: " + e.getMessage());
        }
    }

    @PostMapping("/unsave/{postId}")
    @ResponseBody
    public ResponseEntity<String> unsavePost(@PathVariable("postId") Long postId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal() instanceof String) {
            return ResponseEntity.status(401).body("Authentication required");
        }

        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            userService.removePostFromUserSavedPosts(postId, userDetails.getId());
            return ResponseEntity.ok("Post unsaved successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error unsaving post: " + e.getMessage());
        }
    }

}

package com.mb.reddit.controller;

import com.mb.reddit.dto.CommunityBasicDTO;
import com.mb.reddit.dto.PostWithVotesDTO;
import com.mb.reddit.entity.*;
import com.mb.reddit.repository.CommentRepository;
import com.mb.reddit.repository.CommentVoteRepository;
import com.mb.reddit.service.*;
import com.mb.reddit.entity.User;
import com.mb.reddit.service.CommunityService;
import com.mb.reddit.service.FlairService;
import com.mb.reddit.service.PostService;
import com.mb.reddit.service.TopicService;
import com.mb.reddit.entity.Topic;

import com.mb.reddit.service.PostVoteService;
import com.mb.reddit.service.implementation.UserServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.Authentication;
import com.mb.reddit.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.*;

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
    private final CommentVoteRepository commentVoteRepository;
    private final TopicService topicService;

    public PostController(PostService postService, UserService userService, CommunityService communityService,
                          FlairService flairService, CommentService commentService, PostVoteService postVoteService,
                          NotificationService notificationService, UserServiceImpl userServiceImpl,
                          CommentVoteRepository commentVoteRepository, TopicService topicService) {
        this.postService = postService;
        this.commentService = commentService;
        this.postVoteService = postVoteService;
        this.userService = userService;
        this.communityService = communityService;
        this.flairService = flairService;
        this.userServiceImpl = userServiceImpl;
        this.notificationService = notificationService;
        this.commentVoteRepository = commentVoteRepository;
        this.topicService = topicService;
    }

    @GetMapping("/new-post")
    public String getCreatePostForm(@RequestParam(name = "c", required = false) Long communityId, Model model) {

        List<CommunityBasicDTO> communities = communityService.findCommunitiesUserCanPost();

        model.addAttribute("communities", communities);

        Community selectedCommunity = null;
        List<Flair> flairs = List.of();

        if(communityId != null) {
            selectedCommunity = communityService.getCommunityById(communityId);
        }

        model.addAttribute("notificationCount", 0);
        model.addAttribute("selectedCommunity", selectedCommunity);
        model.addAttribute("flairs", flairs);
        model.addAttribute("postForm", new Post());


        return "create-post";
    }

    @PostMapping("/new-post")
    public String createPost(@ModelAttribute("post") Post post, @RequestParam Long communityId, @RequestParam(value = "file", required = false) MultipartFile file) {
        postService.createPost(post, communityId, file);

        return "redirect:/";
    }

    @GetMapping("/")
    public String getAllPosts(@RequestParam(defaultValue = "0") int pageNumber, 
                         @RequestParam(defaultValue = "10") int pageSize, 
                         @RequestParam(required = false) String sort, 
                         @RequestParam(required = false) String time, 
                         @RequestParam(required = false) String keyword,
                         @RequestParam(required = false) Long topicId,
                         @RequestParam(required = false) List<Long> topicIds,
                         Model model) {

    Page<PostWithVotesDTO> posts;

    if (topicId != null) {
        posts = postService.getPostsByTopicId(topicId, pageNumber, pageSize);
    } else if (topicIds != null && !topicIds.isEmpty()) {
        posts = postService.getPostsByTopicIds(topicIds, pageNumber, pageSize);
    } else {
        posts = postService.getAllPost(pageNumber, pageSize, sort, time, keyword);
    }

    List<Community> joinedCommunities = communityService.findUserJoinedCommunities();
    List<Community> recentCommunities = joinedCommunities.stream().limit(5).toList().reversed();
    List<PostWithVotesDTO> latest10Posts = posts.getContent();
    Integer notificationCount = notificationService.getNotificationCount();

    List<Topic> topics = topicService.getAllTopics();
    model.addAttribute("topics", topics);

    model.addAttribute("notificationCount", notificationCount);
    model.addAttribute("posts", latest10Posts);
    model.addAttribute("recentPosts", latest10Posts);
    model.addAttribute("communities", joinedCommunities);
    model.addAttribute("recentCommunities", recentCommunities);
    model.addAttribute("hasNext", posts.hasNext());

    model.addAttribute("selectedSort", sort != null ? sort : "");
    model.addAttribute("time", time != null ? time : "");
    model.addAttribute("keyword", keyword != null ? keyword : "");
    model.addAttribute("topicId", topicId);
    model.addAttribute("topicIds", topicIds);
    model.addAttribute("hideFilters", "popular".equalsIgnoreCase(sort));
    
    return "home";
}

    @GetMapping("/scroll")
    public String getMorePosts(@RequestParam(defaultValue = "0") int pageNumber, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(required = false) String sort, @RequestParam(required = false) String time, @RequestParam(required = false) String keyword, Model model) {

        Page<PostWithVotesDTO> posts = postService.getAllPost(pageNumber, pageSize, sort, time, keyword);

        model.addAttribute("posts", posts.getContent());
        model.addAttribute("hasNext", posts.hasNext());

        model.addAttribute("selectedSort", sort != null ? sort : "");
        model.addAttribute("time", time != null ? time : "");
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("hideFilters", "popular".equalsIgnoreCase(sort));

        return "home";
    }

    @GetMapping("/posts/{postId}")
    public String getPostById(@PathVariable Long postId, Model model, Authentication authentication) {
        Post post = postService.getPostById(postId);
        List<Comment> topLevelComments = commentService.getTopLevelComments(postId);
        int postVoteCount = postService.getPostVotesByPostId(postId);
        int commentCount = topLevelComments.size();

        Boolean currentUserVote = null;
        if(authentication != null && authentication.isAuthenticated()) {
            currentUserVote = postVoteService.getVoteStatusByPostId(postId);
        }
        Community community = post.getCommunity();
        User owner = community.getCreator();


        Map<Long, Integer> commentVotes = new HashMap<>();
        Map<Long, Boolean> userCommentVotes = new HashMap<>();

        boolean isAuthenticated = authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String);

        model.addAttribute("isAuthenticated", isAuthenticated);
        boolean isJoined = false;
        if(isAuthenticated) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            isJoined = community.getMembers().stream().anyMatch(member -> member.getId().equals(userDetails.getId()));
        }
        boolean isSaved = false;
        if(isAuthenticated) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            if(userDetails != null) {
                isSaved = userService.isPostSavedByUser(postId, userDetails.getId());
            }
            populateCommentVotes(topLevelComments, commentVotes);
            populateUserCommentVotes(topLevelComments, userCommentVotes, userDetails.getId());
        }
        else {
            populateCommentVotes(topLevelComments, commentVotes);
        }

        boolean isOwner = post.getAuthor().getUsername().equals(authentication.getName());

        model.addAttribute("userCommentVotes", userCommentVotes);
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("isSaved", isSaved);
        model.addAttribute("community", community);
        model.addAttribute("owner", owner);
        model.addAttribute("hasJoined", isJoined);
        model.addAttribute("post", post);
        model.addAttribute("topLevelComments", topLevelComments);
        model.addAttribute("commentVotes", commentVotes);
        model.addAttribute("postVoteCount", postVoteCount);
        model.addAttribute("commentCount", commentCount);
        model.addAttribute("currentUserVote", currentUserVote);
        model.addAttribute("notificationCount", 0);
        return "view-post";
    }

    @PostMapping("/posts/delete/{postId}")
    public String deletePostById(@PathVariable("postId") Long postId) {
        postService.deletePost(postId);

        return "redirect:/";
    }

    @GetMapping("/posts/edit/{postId}")
    public String editPostForm(@PathVariable(name = "postId") Long postId, Model model) {

        PostWithVotesDTO postWithVotesDTO = postService.getPostWithVotesByPostId(postId);

        model.addAttribute("currentPost", postWithVotesDTO);
        return "edit-post";
    }

    @PostMapping("/posts/edit/{postId}")
    public String editPost(@ModelAttribute("currentPost") PostWithVotesDTO updatedPost, @RequestParam(name = "file", required = false) MultipartFile file, @RequestParam(name = "removeMedia", defaultValue = "false") boolean removeMedia) {

        postService.updatePost(updatedPost, file, removeMedia);
        return "redirect:/";
    }

    @GetMapping("/user/{userId}/posts")
    public String getUserPosts(@PathVariable Long userId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() instanceof String) {
            return "redirect:/user/login";
        }
        System.out.println(userId + "===========================");
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Page<PostWithVotesDTO> posts = postService.getPostsByUserId(userId, page, size);

        model.addAttribute("posts", posts);
        model.addAttribute("hasNext", posts.hasNext());
        return "fragments/posts :: postSection";
    }

    @GetMapping("/user/{userId}/upvoted")
    public String getUserUpVotedPosts(@PathVariable Long userId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() instanceof String) {
            return "redirect:/user/login";
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Page<PostWithVotesDTO> posts = postService.getUpvotedPostsByUserId(userDetails.getId(), page, size);

        model.addAttribute("posts", posts.getContent());
        model.addAttribute("hasNext", posts.hasNext());
        return "fragments/posts :: postSection";
    }

    @GetMapping("/user/{userId}/downvoted")
    public String getUserDownVotedPosts(@PathVariable Long userId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() instanceof String) {
            return "redirect:/user/login";
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Page<PostWithVotesDTO> posts = postService.getDownVotedPostsByUserId(userDetails.getId(), page, size);

        model.addAttribute("posts", posts);
        model.addAttribute("hasNext", posts.hasNext());
        return "fragments/posts :: postSection";
    }

    @GetMapping("/user/{userId}/saved")
    public String getUserSavedPosts(@PathVariable Long userId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() instanceof String) {
            return "redirect:/user/login";
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        Page<PostWithVotesDTO> posts = postService.getSavedPostsByUserId(userDetails.getId(), page, size);

        model.addAttribute("posts", posts.getContent());
        model.addAttribute("hasNext", posts.hasNext());
        return "fragments/posts :: postSection";
    }

    @PostMapping("/save/{postId}")
    @ResponseBody
    public ResponseEntity<String> savePost(@PathVariable("postId") Long postId, Authentication authentication) {
        if(authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() instanceof String) {
            return ResponseEntity.status(401).body("Authentication required");
        }

        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            userService.addPostToUserSavedPosts(postId, userDetails.getId());
            return ResponseEntity.ok("Post saved successfully");
        } catch(Exception e) {
            return ResponseEntity.badRequest().body("Error saving post: " + e.getMessage());
        }
    }

    @PostMapping("/unsave/{postId}")
    @ResponseBody
    public ResponseEntity<String> unsavePost(@PathVariable("postId") Long postId, Authentication authentication) {
        if(authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() instanceof String) {
            return ResponseEntity.status(401).body("Authentication required");
        }

        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            userService.removePostFromUserSavedPosts(postId, userDetails.getId());
            return ResponseEntity.ok("Post unsaved successfully");
        } catch(Exception e) {
            return ResponseEntity.badRequest().body("Error unsaving post: " + e.getMessage());
        }
    }

    @GetMapping("/topic/{topicId}")
    public String getPostsByTopic(@PathVariable Long topicId,
                             @RequestParam(defaultValue = "0") int pageNumber,
                             @RequestParam(defaultValue = "10") int pageSize,
                             Model model) {
    
        Page<PostWithVotesDTO> posts = postService.getPostsByTopicId(topicId, pageNumber, pageSize);
        Topic topic = topicService.getTopicById(topicId);
        
        List<Community> joinedCommunities = communityService.findUserJoinedCommunities();
        Integer notificationCount = notificationService.getNotificationCount();
        
        model.addAttribute("posts", posts.getContent());
        model.addAttribute("topic", topic);
        model.addAttribute("communities", joinedCommunities);
        model.addAttribute("notificationCount", notificationCount);
        model.addAttribute("hasNext", posts.hasNext());
        model.addAttribute("currentPage", pageNumber);
        model.addAttribute("totalPages", posts.getTotalPages());
        
        return "topic-posts";
    }

    private void populateCommentVotes(List<Comment> comments, Map<Long, Integer> commentVotes) {
        if(comments == null || comments.isEmpty()) {
            return;
        }

        for(Comment comment : comments) {
            int voteCount = commentService.getVoteCountForComment(comment.getId());
            commentVotes.put(comment.getId(), voteCount);

            if(comment.getReplies() != null && !comment.getReplies().isEmpty()) {
                populateCommentVotes(comment.getReplies(), commentVotes);
            }
        }
    }
    private void populateUserCommentVotes(List<Comment> comments, Map<Long, Boolean> userCommentVotes, Long userId) {
        if(comments == null || comments.isEmpty()) {
            return;
        }

        for(Comment comment : comments) {
            Optional<CommentVote> vote = commentVoteRepository.findByUserIdAndCommentId(userId, comment.getId());
            if(vote.isPresent()) {
                userCommentVotes.put(comment.getId(), vote.get().getIsLike());
            }

            if(comment.getReplies() != null && !comment.getReplies().isEmpty()) {
                populateUserCommentVotes(comment.getReplies(), userCommentVotes, userId);
            }
        }
    }


}

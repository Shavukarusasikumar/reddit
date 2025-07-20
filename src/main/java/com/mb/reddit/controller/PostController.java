package com.mb.reddit.controller;

import com.mb.reddit.entity.Comment;
import com.mb.reddit.entity.Community;
import com.mb.reddit.entity.Flair;
import com.mb.reddit.entity.Post;
import com.mb.reddit.service.CommentService;
import com.mb.reddit.entity.User;
import com.mb.reddit.service.CommunityService;
import com.mb.reddit.service.FlairService;
import com.mb.reddit.service.PostService;

import com.mb.reddit.service.PostVoteService;
import org.springframework.security.core.Authentication;
import com.mb.reddit.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
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


    public PostController(PostService postService,UserService userService, CommunityService communityService, FlairService flairService,CommentService commentService,PostVoteService postVoteService) {
        this.postService = postService;
        this.commentService = commentService;
        this.postVoteService = postVoteService;
        this.userService = userService;
        this.communityService = communityService;
        this.flairService = flairService;
    }

    @GetMapping("/new-post")
    public String getCreatePostForm(@RequestParam(name = "c", required = false) Long communityId, Model model) {
        List<Community> communities = communityService.findCommunitiesUserCanPost();
        model.addAttribute("communities", communities);

        Community selected = null;
        List<Flair> flairs = List.of();

        if(communityId != null) {
            selected = communityService.getCommunityById(communityId);
            flairs = flairService.getAllFlairsByCommunityId(communityId);
        }
        model.addAttribute("selectedCommunity", selected);
        model.addAttribute("flairs", flairs);

        model.addAttribute("postForm", new Post());

        return "create-post";
    }

    @PostMapping("/new-post")
    public String createPost(@ModelAttribute("post") Post post, @RequestParam Long communityId,
                             MultipartFile file){
        postService.createPost(post,communityId ,file);

        return "redirect:/home";
    }

    @GetMapping("/posts")
    public String getAllPosts(@RequestParam(defaultValue = "0", required = false) int pageNumber,
                              @RequestParam(defaultValue = "10", required = false) int pageSize,
                              @RequestParam(defaultValue = "createdAt", required = false) String sortBy,
                              Model model) {
        Page<Post> posts = postService.getAllPost(pageNumber, pageSize, sortBy);
        List<Community> joinedCommunities = communityService.getAllCommunities();
//                .getJoinedCommunitiesByUserId(userService.getCurrentUser().getId());

        List<Community> recentCommunities = joinedCommunities.stream()
                .limit(5)
                .toList();

        List<Post> recentPosts = new ArrayList<>();

        for (Community joinedCommunity : joinedCommunities) {
            recentPosts.addAll(joinedCommunity.getPosts());
        }

        recentPosts.sort(Comparator.comparing(Post::getCreatedAt).reversed());

        List<Post> latest10Posts = posts.stream()
                .limit(10)
                .toList();

        model.addAttribute("recentPosts", latest10Posts);
        model.addAttribute("communities", joinedCommunities);
        model.addAttribute("recentCommunities", recentCommunities);
        model.addAttribute("posts", posts);
        model.addAttribute("hasNext", posts.hasNext());

        return "home";
    }


    @GetMapping("/posts/scroll")
    public String getMorePosts(@RequestParam(defaultValue = "0") int pageNumber,
                               @RequestParam(defaultValue = "10") int pageSize,
                               @RequestParam(defaultValue = "createdAt") String sortBy,
                               Model model) {
        Page<Post> posts = postService.getAllPost(pageNumber, pageSize, sortBy);
        model.addAttribute("posts", posts);
        model.addAttribute("hasNext", posts.hasNext());
        return "fragments/posts :: postSection";
    }

    @GetMapping("/posts/{postId}")
    public String getPostById(@PathVariable Long postId, Model model, Authentication authentication) {
        Post post = postService.getPostById(postId);
        List<Comment> topLevelComments = commentService.getTopLevelComments(postId);
        int postVoteCount = postService.getPostVotesByPostId(postId);
        int commentCount = topLevelComments.size();

        Boolean currentUserVote = postVoteService.getVoteStatusByPostId(postId);
        Community community = post.getCommunity();
        User owner = community.getCreator();

        Map<Long, Integer> commentVotes = new HashMap<>();
        for (Comment comment : topLevelComments) {
            int voteCount = commentService.getVoteCountForComment(comment.getId());
            commentVotes.put(comment.getId(), voteCount);
        }

        boolean isAuthenticated = authentication != null && authentication.isAuthenticated();
        model.addAttribute("isAuthenticated", isAuthenticated);
        boolean isJoined = userService.hasUserJoinedCommunity(community);

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
}

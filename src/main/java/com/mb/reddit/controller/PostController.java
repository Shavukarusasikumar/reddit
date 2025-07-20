package com.mb.reddit.controller;

import com.mb.reddit.entity.Community;
import com.mb.reddit.entity.Post;
import com.mb.reddit.service.CommunityService;
import com.mb.reddit.service.PostService;

import com.mb.reddit.service.UserService;
import org.springframework.ui.Model;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Controller
public class PostController {

    public final PostService postService;
    private final UserService userService;
    private final CommunityService communityService;


    public PostController(PostService postService, UserService userService, CommunityService communityService) {
        this.postService = postService;
        this.userService = userService;
        this.communityService = communityService;
    }

    @GetMapping("/new-post")
    public String getCreatePostForm(Model model){
        model.addAttribute("post", new Post());

        return "create-post";
    }

    @PostMapping("/new-post")
    public String createPost(@ModelAttribute("post") Post post, @RequestParam Long communityId,
                             MultipartFile file){
        postService.createPost(post,communityId ,file);

        return "redirect:/home";
    }

    @GetMapping
    public String getAllPosts(@RequestParam(defaultValue = "0", required = false) int pageNumber,
                              @RequestParam(defaultValue = "10", required = false) int pageSize,
                              @RequestParam(defaultValue = "publishedAt", required = false) String sortBy,
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

        return "home";
    }

    @GetMapping("/posts/{postId}")
    public String getPostById(@PathVariable Long postId, Model model) {
        Post post = postService.getPostById(postId);

        model.addAttribute("post", post);

        return "view-post";
    }

    @PostMapping("/delete/post/{postId}")
    public String deletePostById(@PathVariable("postId") Long postId) {
        postService.deletePost(postId);

        return "redirect:/";
    }
}

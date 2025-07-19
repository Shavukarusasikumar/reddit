package com.mb.reddit.controller;

import com.mb.reddit.entity.Post;
import com.mb.reddit.service.PostService;

import org.springframework.ui.Model;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PostController {

    public final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
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

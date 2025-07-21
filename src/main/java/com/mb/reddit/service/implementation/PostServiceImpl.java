package com.mb.reddit.service.implementation;

import com.mb.reddit.dto.PostWithVotesDTO;
import com.mb.reddit.entity.*;
import com.mb.reddit.repository.*;
import com.mb.reddit.service.PostService;

import com.mb.reddit.utils.TimeAgoUtils;
import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final CloudinaryService cloudinaryService;
    private final PostVoteRepository postVoteRepository;
    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;

    public PostServiceImpl(PostRepository postRepository, CommentRepository commentRepository, CloudinaryService cloudinaryService, PostVoteRepository postVoteRepository, CommunityRepository communityRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.cloudinaryService = cloudinaryService;
        this.postVoteRepository = postVoteRepository;
        this.communityRepository = communityRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Post getPostById(Long postId) {
        return postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found with id " + postId));
    }

    @Override
    public Page<PostWithVotesDTO> getAllPost(int pageNumber, int pageSize, String sortby) {
        Sort sort = Sort.by(sortby).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        long start = System.currentTimeMillis();
        Page<PostWithVotesDTO> postsPage = postRepository.findAllPublicPublishedPosts(pageable);
        long dbTime = System.currentTimeMillis();

        System.out.println("Service: DB fetch time: " + (dbTime - start) + " ms");
        return postsPage;
    }


    @Override
    public List<Comment> getCommentsByPostId(Long postId) {
        return commentRepository.getCommentsByPostId(postId);
    }

    @Override
    @Transactional
    public void deletePost(Long postId) {
        postRepository.deleteById(postId);
    }

    @Transactional
    @Override
    public Post createPost(Post post, Long communityId, MultipartFile media) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findUserByUsername(authentication.getName());

        if(media != null) {
            try {
                String mediaUrl = cloudinaryService.uploadFile(media);
                post.setMediaUrl(mediaUrl);
                post.setCreatedAt(LocalDateTime.now());
                post.setIsPublished(true);
            } catch(IOException exception) {
                throw new RuntimeException("Failed to upload media", exception);
            }
        }

        Community community = communityRepository.findById(communityId).orElseThrow(() -> new RuntimeException("Community not found " + communityId));

        post.setAuthor(currentUser);
        post.setCreatedAt(LocalDateTime.now());
        post.setCommunity(community);

        return postRepository.save(post);
    }

    @Override
    @Transactional
    public Post updatePost(Post updatedPost) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Post oldPost = postRepository.findById(updatedPost.getId()).orElseThrow();

        if(!username.equals(oldPost.getAuthor().getUsername())) {
            throw new RuntimeException("UnAuthorized");
        }

        updatedPost.setCreatedAt(oldPost.getCreatedAt());
        updatedPost.setUpdatedAt(LocalDateTime.now());

        return postRepository.save(updatedPost);
    }

    @Override
    public Integer getPostVotesByPostId(Long postId) {
        Integer upVoteCount = postVoteRepository.countUpvoteByPostId(postId);
        Integer downVoteCount = postVoteRepository.countDownvoteByPostId(postId);

        return upVoteCount - downVoteCount;
    }

    @Override
    public Page<Post> getPostsByUserId(Long userId, int pageNumber, int size) {
        Pageable pageable = PageRequest.of(pageNumber, size);
        Page<Post> postsPage =  postRepository.getPostsByUserId(userId, pageable);
        postsPage.forEach(post -> {
            String showTime = TimeAgoUtils.getTimeAgo(post.getCreatedAt());
            post.setShowTime(showTime);
        });

        return postsPage;
    }

    @Override
    public Page<Post> getUpvotedPostsByUserId(Long userId, int pageNumber, int size) {
        Pageable pageable = PageRequest.of(pageNumber, size);
        Boolean isLike = true;
        Page<Post> postsPage = postRepository.getVotedPostByUserId(userId, isLike ,pageable);

        postsPage.forEach(post -> {
            String showTime = TimeAgoUtils.getTimeAgo(post.getCreatedAt());
            post.setShowTime(showTime);
        });

        return postsPage;
    }

    @Override
    public Page<Post> getDownVotedPostsByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Boolean isLike = false;

        Page<Post> postsPage = postRepository.getVotedPostByUserId(userId, isLike ,pageable);

        postsPage.forEach(post -> {
            String showTime = TimeAgoUtils.getTimeAgo(post.getCreatedAt());
            post.setShowTime(showTime);
        });

        return postsPage;
    }

    @Override
    public Page<Post> getSavedPostsByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Post> postsPage = postRepository.getSavedPostsByUserId(userId ,pageable);

        postsPage.forEach(post -> {
            String showTime = TimeAgoUtils.getTimeAgo(post.getCreatedAt());
            post.setShowTime(showTime);
        });

        return postsPage;
    }

    @Override
    public Page<Post> getPostsByCommunityId(Long communityId, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        return postRepository.getPostsByCommunityId(communityId, pageable);
    }
}
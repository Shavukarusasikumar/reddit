package com.mb.reddit.service.implementation;

import com.mb.reddit.dto.PostWithVotesDTO;
import com.mb.reddit.entity.*;
import com.mb.reddit.repository.*;
import com.mb.reddit.service.PostService;

import com.mb.reddit.utils.TimeAgoUtils;
import jakarta.transaction.Transactional;

import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final CloudinaryService cloudinaryService;
    private final PostVoteRepository postVoteRepository;
    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;
    private final UserServiceImpl userService;

    public PostServiceImpl(PostRepository postRepository, CommentRepository commentRepository, UserServiceImpl userService, CloudinaryService cloudinaryService, PostVoteRepository postVoteRepository, CommunityRepository communityRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.cloudinaryService = cloudinaryService;
        this.postVoteRepository = postVoteRepository;
        this.communityRepository = communityRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Override
    public Post getPostById(Long postId) {
        return postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found with id " + postId));
    }

    @Override
    public Page<PostWithVotesDTO> getAllPost(int pageNumber, int pageSize, String sortBy, boolean rising, boolean top, boolean isNew, boolean popular, String keyword) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        LocalDateTime timeThreshold = LocalDateTime.now().minusHours(96);

        Page<PostWithVotesDTO> page;

        if(keyword != null && !keyword.isEmpty()) {
            page = postRepository.searchPostsByKeyword(keyword, pageable);
        }
        else if(top) {
            page = postRepository.findTopPosts(pageable);
        }
        else if(rising) {
            page = postRepository.findRisingPosts(timeThreshold, pageable);
        }
        else if(popular) {
            page = postRepository.findPopularPosts(pageable);
        }
        else {
            page = postRepository.findPublicPosts(pageable);
        }


        Long userId = getLoggedInUserId(authentication);
        if(userId == null) {
            return page;
        }

        List<PostWithVotesDTO> postList = page.getContent();
        List<Long> postIds = postList.stream().map(PostWithVotesDTO::getId).toList();

        if(postIds.isEmpty()) {
            return page;
        }

        List<PostVote> userVotes = postVoteRepository.findByUserIdAndPostIds(userId, postIds);

        Map<Long, Boolean> voteMap = new HashMap<>();
        for(PostVote vote : userVotes) {
            voteMap.put(vote.getPost().getId(), vote.getIsLike());
        }

        for(PostWithVotesDTO post : postList) {
            post.setIsLiked(voteMap.get(post.getId()));
        }

        return page;
    }

    private Long getLoggedInUserId(Authentication authentication) {
        if(authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return null;
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails != null ? userDetails.getId() : null;
    }

    @Override
    public List<Comment> getCommentsByPostId(Long postId) {
        return commentRepository.getCommentsByPostId(postId);
    }

    @Override
    @Transactional
    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));

        for(User user : post.getSavedByUser()) {
            user.getSavedPosts().remove(post);
        }

        postRepository.delete(post);
    }

    @Transactional
    @Override
    public Post createPost(Post post, Long communityId, MultipartFile media) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findUserByUsername(authentication.getName());

        if(media != null && !media.isEmpty()) {
            try {
                String mediaUrl = cloudinaryService.uploadFile(media);
                post.setMediaUrl(mediaUrl);
                post.setCreatedAt(LocalDateTime.now());
                post.setIsPublished(true);
            } catch(IOException exception) {
                throw new RuntimeException("Failed to upload media", exception);
            }
        }

        post.setCreatedAt(LocalDateTime.now());
        post.setIsPublished(true);

        Community community = communityRepository.findById(communityId).orElseThrow(() -> new RuntimeException("Community not found " + communityId));

        post.setAuthor(currentUser);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        post.setCommunity(community);

        return postRepository.save(post);
    }

    @Override
    @Transactional
    public Post updatePost(PostWithVotesDTO updatedPost, MultipartFile media, boolean removeMedia) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Post oldPost = postRepository.findById(updatedPost.getId()).orElseThrow();

        if(!username.equals(oldPost.getAuthor().getUsername())) {
            throw new RuntimeException("Unauthorized");
        }

        oldPost.setContent(updatedPost.getContent());
        oldPost.setTitle(updatedPost.getTitle());

        if(removeMedia) {
            oldPost.setMediaUrl(null);
        }
        // If removeMedia checkbox is checked

        if(media != null && !media.isEmpty()) {
            try {
                String mediaUrl = cloudinaryService.uploadFile(media);
                oldPost.setMediaUrl(mediaUrl);
            } catch(IOException exception) {
                throw new RuntimeException("Failed to upload media", exception);
            }
        }

        return postRepository.save(oldPost);
    }

    @Override
    public Integer getPostVotesByPostId(Long postId) {
        Integer upVoteCount = postVoteRepository.countUpvoteByPostId(postId);
        Integer downVoteCount = postVoteRepository.countDownvoteByPostId(postId);

        return upVoteCount - downVoteCount;
    }

    @Override
    public Page<PostWithVotesDTO> getPostsByUserId(Long userId, int pageNumber, int size) {
        Pageable pageable = PageRequest.of(pageNumber, size);
        Page<PostWithVotesDTO> postsPage = postRepository.getPostDTOsByUserId(userId, pageable);
        postsPage.forEach(post -> {
            String showTime = TimeAgoUtils.getTimeAgo(post.getCreatedAt());
            post.setShowTime(showTime);
        });

        return postsPage;
    }

    @Override
    public Page<PostWithVotesDTO> getUpvotedPostsByUserId(Long userId, int pageNumber, int size) {
        Pageable pageable = PageRequest.of(pageNumber, size);
        Boolean isLike = true;
        Page<PostWithVotesDTO> postsPage = postRepository.getVotedPostsDTO(userId, isLike, pageable);

        System.out.println(postsPage);
        postsPage.forEach(post -> {
            System.out.println(post);
            String showTime = TimeAgoUtils.getTimeAgo(post.getCreatedAt());
            post.setShowTime(showTime);
        });

        return postsPage;
    }

    @Override
    public Page<PostWithVotesDTO> getDownVotedPostsByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Boolean isLike = false;

        Page<PostWithVotesDTO> postsPage = postRepository.getVotedPostsDTO(userId, isLike, pageable);

        postsPage.forEach(post -> {
            String showTime = TimeAgoUtils.getTimeAgo(post.getCreatedAt());
            post.setShowTime(showTime);
        });

        return postsPage;
    }

    @Override
    public Page<PostWithVotesDTO> getSavedPostsByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<PostWithVotesDTO> postsPage = postRepository.getSavedPostDTOsByUserId(userId, pageable);

        postsPage.forEach(post -> {
            String showTime = TimeAgoUtils.getTimeAgo(post.getCreatedAt());
            post.setShowTime(showTime);
        });

        return postsPage;
    }

    @Override
    public PostWithVotesDTO getPostWithVotesByPostId(Long postId) {
        return postRepository.getPostWithVotesByPostId(postId);
    }

    @Override
    public Page<Post> getPostsByCommunityId(Long communityId, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        return postRepository.getPostsByCommunityId(communityId, pageable);
    }
}
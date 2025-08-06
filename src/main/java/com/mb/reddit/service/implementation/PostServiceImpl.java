package com.mb.reddit.service.implementation;

import com.mb.reddit.dto.PostWithVotesDTO;
import com.mb.reddit.entity.*;
import com.mb.reddit.exception.custom.MediaUploadError;
import com.mb.reddit.exception.custom.PostNotFoundException;
import com.mb.reddit.exception.custom.UnauthorizedAccessException;
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
        return postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException("Post not found with id " + postId));
    }

    public Page<PostWithVotesDTO> getAllPost(int pageNumber, int pageSize, String sort, String time, String keyword) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        LocalDateTime[] timeRange = getTimeRange(time);
        LocalDateTime startDate = timeRange[0];
        LocalDateTime endDate = timeRange[1];

        Long userId = getLoggedInUserId(authentication);

        Page<PostWithVotesDTO> page;

        if (keyword != null && !keyword.trim().isEmpty()) {
            page = postRepository.searchPostsByKeyword(keyword, userId, pageable);
        } else {
            String sortOption = (sort != null) ? sort : "";

            page = switch (sortOption) {
                case "top" -> postRepository.findTopPosts(startDate, endDate, userId, pageable);
                case "hot" -> postRepository.findHotPosts(startDate, endDate, userId, pageable);
                case "rising" -> {
                    LocalDateTime timeThreshold = LocalDateTime.now().minusHours(96);
                    yield postRepository.findRisingPosts(timeThreshold, userId, pageable);
                }
                case "popular" -> postRepository.findPopularPosts(userId, pageable);
                default -> postRepository.findPublicPosts(userId, pageable);
            };
        }

        if (userId == null) return page;

        List<PostWithVotesDTO> postList = page.getContent();
        List<Long> postIds = postList.stream().map(PostWithVotesDTO::getId).toList();
        if (postIds.isEmpty()) return page;

        List<PostVote> userVotes = postVoteRepository.findByUserIdAndPostIds(userId, postIds);

        Map<Long, Boolean> voteMap = new HashMap<>();
        for (PostVote vote : userVotes) {
            voteMap.put(vote.getPost().getId(), vote.getIsLike());
        }

        for (PostWithVotesDTO post : postList) {
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
        long start = System.currentTimeMillis(); // Start total timer

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findUserByUsername(authentication.getName());

        long userFetchEnd = System.currentTimeMillis();
        System.out.println("⏱️ User fetch time: " + (userFetchEnd - start) + " ms");

        if (media != null && !media.isEmpty()) {
            long mediaStart = System.currentTimeMillis();
            try {
                String mediaUrl = cloudinaryService.uploadMedia(media);
                String mediaType = media.getContentType().startsWith("video/") ? "video" : "image";
                post.setMediaType(mediaType);
                post.setMediaUrl(mediaUrl);
                post.setCreatedAt(LocalDateTime.now());
                post.setIsPublished(true);
            } catch (IOException exception) {
                throw new MediaUploadError("Failed to upload media: " + exception.getMessage());
            }
            long mediaEnd = System.currentTimeMillis();
            System.out.println("⏱️ Media upload + processing time: " + (mediaEnd - mediaStart) + " ms");
        }

        post.setCreatedAt(LocalDateTime.now());
        post.setIsPublished(true);

        long communityStart = System.currentTimeMillis();
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("Community not found " + communityId));
        long communityEnd = System.currentTimeMillis();
        System.out.println("⏱️ Community fetch time: " + (communityEnd - communityStart) + " ms");

        post.setAuthor(currentUser);
        post.setUpdatedAt(LocalDateTime.now());
        post.setCommunity(community);

        long saveStart = System.currentTimeMillis();
        Post savedPost = postRepository.save(post);
        long saveEnd = System.currentTimeMillis();
        System.out.println("⏱️ DB save time: " + (saveEnd - saveStart) + " ms");

        long totalEnd = System.currentTimeMillis();
        System.out.println("✅ Total createPost time: " + (totalEnd - start) + " ms");

        return savedPost;
    }

    @Override
    @Transactional
    public Post updatePost(PostWithVotesDTO updatedPost, MultipartFile media, boolean removeMedia) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Post oldPost = postRepository.findById(updatedPost.getId()).orElseThrow();

        if (!username.equals(oldPost.getAuthor().getUsername())) {
            throw new UnauthorizedAccessException("Cannot update Post");
        }

        oldPost.setContent(updatedPost.getContent());
        oldPost.setTitle(updatedPost.getTitle());

        String linkUrl = updatedPost.getLinkUrl();

        if (linkUrl != null && !linkUrl.isBlank()) {
            oldPost.setLinkUrl(linkUrl);
            oldPost.setMediaUrl(null);
            oldPost.setMediaType(null);
        } else if (media != null && !media.isEmpty()) {
            try {
                String mediaUrl = cloudinaryService.uploadMedia(media);
                String mediaType = media.getContentType().startsWith("video/") ? "video" : "image";
                oldPost.setMediaUrl(mediaUrl);
                oldPost.setMediaType(mediaType);
                oldPost.setLinkUrl(null);
            } catch (IOException exception) {
                throw new MediaUploadError("Failed to upload media: " + exception.getMessage());
            }
        } else if (removeMedia) {
            oldPost.setMediaUrl(null);
            oldPost.setMediaType(null);
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

    private LocalDateTime[] getTimeRange(String time) {
        if (time == null || time.isBlank() || time.equals("all")) {
            return new LocalDateTime[]{ null, null };
        }

        LocalDateTime now = LocalDateTime.now();

        return switch (time) {
            case "today" -> new LocalDateTime[] {
                    now.toLocalDate().atStartOfDay(),
                    now.toLocalDate().atStartOfDay().plusDays(1).minusSeconds(1)
            };
            case "yesterday" -> new LocalDateTime[] {
                    now.toLocalDate().minusDays(1).atStartOfDay(),
                    now.toLocalDate().atStartOfDay().minusSeconds(1)
            };
            case "month" -> new LocalDateTime[] {
                    now.withDayOfMonth(1).toLocalDate().atStartOfDay(),
                    now.withDayOfMonth(1).toLocalDate().atStartOfDay().plusMonths(1).minusSeconds(1)
            };
            case "year" -> new LocalDateTime[] {
                    now.withDayOfYear(1).toLocalDate().atStartOfDay(),
                    now.withDayOfYear(1).toLocalDate().atStartOfDay().plusYears(1).minusSeconds(1)
            };
            default -> new LocalDateTime[] {
                    LocalDateTime.of(1970, 1, 1, 0, 0),
                    LocalDateTime.of(2999, 12, 31, 23, 59, 59)
            };
        };
    }


}
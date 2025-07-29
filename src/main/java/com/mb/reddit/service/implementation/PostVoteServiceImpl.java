package com.mb.reddit.service.implementation;

import com.mb.reddit.entity.*;
import com.mb.reddit.exception.custom.PostNotFoundException;
import com.mb.reddit.repository.NotificationRepository;
import com.mb.reddit.repository.PostRepository;
import com.mb.reddit.repository.PostVoteRepository;
import com.mb.reddit.repository.UserRepository;
import com.mb.reddit.service.PostVoteService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PostVoteServiceImpl implements PostVoteService {

    private final PostVoteRepository postVoteRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final UserServiceImpl userServiceImpl;
    private final NotificationRepository notificationRepository;

    public PostVoteServiceImpl(PostVoteRepository postVoteRepository,
                               PostRepository postRepository,
                               UserRepository userRepository,
                               NotificationRepository notificationRepository,
                               UserServiceImpl userServiceImpl) {
        this.postVoteRepository = postVoteRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.userServiceImpl = userServiceImpl;
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional
    public void addVoteByPostId(Long postId, Boolean isLike) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException("Post not found " + postId));

        User currentUser = userServiceImpl.getCurrentUser();

        PostVote postVote = postVoteRepository.getPostVoteByUserIdAndPostId(currentUser.getId(),
                postId).orElse(new PostVote());

        postVote.setPost(post);
        postVote.setUser(currentUser);
        postVote.setIsLike(isLike);
        postVote.setLikedAt(LocalDateTime.now());

        postVoteRepository.save(postVote);

        if(postVote.getIsLike() && !currentUser.equals(post.getAuthor())){
            String type = "UPVOTE";
            Notification existing = notificationRepository.findTopByRecipientAndPostAndType(
                    post.getAuthor().getId(), post.getId(), type);

            if (existing == null) {
                Notification notification = new Notification();
                notification.setRecipient(post.getAuthor());
                notification.setPost(post);
                notification.setMessage(" upvoted your post: " + post.getTitle());
                notification.setType("UPVOTE");
                notification.setSenderId(currentUser.getId());
                notification.setRead(false);
                notification.setSenderName(currentUser.getUsername());
                notification.setTimestamp(LocalDateTime.now());
                notificationRepository.save(notification);
            }
        }
    }

    @Override
    @Transactional
    public void removeVoteByPostId(Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal() instanceof String) {
        } else{
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long userId = userDetails.getId();

            postVoteRepository.findByUserIdAndPostId(userId, postId)
                    .ifPresent(vote -> {
                        postVoteRepository.delete(vote);
                    });
        }
    }

    @Override
    public Boolean getVoteStatusByPostId(Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal() instanceof String) {
            return null;
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();

        return postVoteRepository.findByUserIdAndPostId(userId, postId)
                .map(PostVote::getIsLike)
                .orElse(null);
    }
}
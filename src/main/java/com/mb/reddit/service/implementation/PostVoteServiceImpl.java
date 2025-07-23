package com.mb.reddit.service.implementation;

import com.mb.reddit.entity.*;
import com.mb.reddit.repository.NotificationRepository;
import com.mb.reddit.repository.PostRepository;
import com.mb.reddit.repository.PostVoteRepository;
import com.mb.reddit.repository.UserRepository;
import com.mb.reddit.service.PostVoteService;
import org.springframework.http.ResponseEntity;
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
        System.out.println("----------------------------Starting Vote Process for Post: " + postId + " IsLike: " + isLike + "-------------------------------------");

        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found " + postId));

        User currentUser = userServiceImpl.getCurrentUser();

        PostVote postVote = postVoteRepository.getPostVoteByUserIdAndPostId(currentUser.getId(),
                postId).orElse(new PostVote());

        System.out.println("----------------------------Found existing vote: " + (postVote.getId() != null) + "-------------------------------------");

        postVote.setPost(post);
        postVote.setUser(currentUser);
        postVote.setIsLike(isLike);
        postVote.setLikedAt(LocalDateTime.now());
        postVoteRepository.save(postVote);

        if(postVote.getIsLike()){
            String type = "UPVOTE";
            Notification existing = notificationRepository.findTopByRecipientAndPostAndType(
                    post.getAuthor().getId(), post.getId(), type);

            if (existing == null) {
                Notification notification = new Notification();
                notification.setRecipient(post.getAuthor());
                notification.setPost(post);
                notification.setMessage(currentUser.getUsername() + " upvoted your post: " + post.getTitle());
                notification.setType("UPVOTE");
                notification.setRead(false);
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
        }else{
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long userId = userDetails.getId();

            postVoteRepository.findByUserIdAndPostId(userId, postId)
                    .ifPresent(vote -> {
                        postVoteRepository.delete(vote);
                        System.out.println("----------------------------Vote Removed ---------------------------------------------");
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


    @Override
    public Integer getPostVotesByPostId(Long postId) {
        Integer upVotes = postVoteRepository.countUpvoteByPostId(postId);
        Integer downVotes = postVoteRepository.countDownvoteByPostId(postId);
        return (upVotes != null ? upVotes : 0) - (downVotes != null ? downVotes : 0);
    }
}
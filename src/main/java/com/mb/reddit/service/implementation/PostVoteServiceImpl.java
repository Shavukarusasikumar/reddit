package com.mb.reddit.service.implementation;

import com.mb.reddit.entity.Notification;
import com.mb.reddit.entity.Post;
import com.mb.reddit.entity.PostVote;
import com.mb.reddit.entity.User;
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

    public PostVoteServiceImpl(PostVoteRepository postVoteRepository, PostRepository postRepository,
                               UserRepository userRepository, UserServiceImpl userServiceImpl,
                               NotificationRepository notificationRepository) {
        this.postVoteRepository = postVoteRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.userServiceImpl = userServiceImpl;
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional
    public void addVoteByPostId(Long postId, Boolean isLike) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found " + postId));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userRepository.findUserByUsername("shruti_dev"); //TODO Change it to username

        PostVote postVote = postVoteRepository.getPostVoteByUserIdAndPostId(currentUser.getId(),
                postId).orElse(new PostVote());

        postVote.setPost(post);
        postVote.setUser(currentUser);
        postVote.setIsLike(isLike);
        postVote.setLikedAt(LocalDateTime.now());
        postVoteRepository.save(postVote);

        if(postVote.getIsLike()){
            String type = "UPVOTE";
            Notification existing = notificationRepository.findTopByRecipientAndPostAndType(post.getAuthor(),
                    post, type);
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
        User currentUser = userServiceImpl.getLoggedInUser();

        if (currentUser == null) {
            return;
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        postVoteRepository.findByUserAndPost(currentUser, post)
                .ifPresent(vote -> {
                    postVoteRepository.delete(vote);
                    System.out.println("----------------------------Vote Removed ---------------------------------------------");
                });
    }

    @Override
    public Boolean getVoteStatusByPostId(Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String username = authentication.getName();
        User currentUser = userRepository.findUserByUsername(username);
        User user = userRepository.findUserByUsername(username);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        return postVoteRepository.findByUserAndPost(currentUser, post)
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
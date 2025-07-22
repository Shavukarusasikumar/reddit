package com.mb.reddit.service.implementation;

import com.mb.reddit.entity.Post;
import com.mb.reddit.entity.PostVote;
import com.mb.reddit.entity.User;
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

    public PostVoteServiceImpl(PostVoteRepository postVoteRepository, PostRepository postRepository,
                               UserRepository userRepository,  UserServiceImpl userServiceImpl) {
        this.postVoteRepository = postVoteRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.userServiceImpl = userServiceImpl;
    }

    @Override
    @Transactional
    public void addVoteByPostId(Long postId, Boolean isLike) {
        System.out.println("----------------------------Starting Vote Process for Post: " + postId + " IsLike: " + isLike + "-------------------------------------");

        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found " + postId));

        User currentuser = userServiceImpl.getLoggedInUser();

        if (currentuser == null) {
            System.out.println("----------------------------Current user is null ---------------------------------------------");
            throw new RuntimeException("Current user is null");
        }

        PostVote postVote = postVoteRepository.getPostVoteByUserIdAndPostId(currentuser.getId(),
                postId).orElse(new PostVote());

        System.out.println("----------------------------Found existing vote: " + (postVote.getId() != null) + "-------------------------------------");

        postVote.setPost(post);
        postVote.setUser(currentuser);
        postVote.setIsLike(isLike);
        postVote.setLikedAt(LocalDateTime.now());
        postVoteRepository.save(postVote);
        System.out.println("----------------------------Vote Saved Successfully ---------------------------------------------");
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
        User currentUser = userServiceImpl.getLoggedInUser();

        if (currentUser == null) {
            return null;
        }

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
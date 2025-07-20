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
import java.util.Optional;

@Service
public class PostVoteServiceImpl implements PostVoteService {

    private final PostVoteRepository postVoteRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostVoteServiceImpl(PostVoteRepository postVoteRepository, PostRepository postRepository, UserRepository userRepository) {
        this.postVoteRepository = postVoteRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void addVoteByPostId(Long postId, Boolean isLike) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findUserByUsername(username);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Optional<PostVote> existingVote = postVoteRepository.findByUserAndPost(user, post);

        if (existingVote.isPresent()) {
            PostVote vote = existingVote.get();

            if (vote.getIsLike().equals(isLike)) {
                postVoteRepository.delete(vote);
                return;
            }

            vote.setIsLike(isLike);
            vote.setLikedAt(LocalDateTime.now());
            postVoteRepository.save(vote);
        } else {
            PostVote newVote = new PostVote();
            newVote.setPost(post);
            newVote.setUser(user);
            newVote.setIsLike(isLike);
            newVote.setLikedAt(LocalDateTime.now());
            postVoteRepository.save(newVote);
        }
    }

    @Override
    @Transactional
    public void removeVoteByPostId(Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findUserByUsername(username);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        postVoteRepository.findByUserAndPost(user, post)
                .ifPresent(postVoteRepository::delete);
    }

    @Override
    public Boolean getVoteStatusByPostId(Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String username = authentication.getName();
        User user = userRepository.findUserByUsername(username);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        return postVoteRepository.findByUserAndPost(user, post)
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
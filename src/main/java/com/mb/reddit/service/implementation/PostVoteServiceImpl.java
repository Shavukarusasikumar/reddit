package com.mb.reddit.service.implementation;

import com.mb.reddit.entity.Post;
import com.mb.reddit.entity.PostVote;
import com.mb.reddit.entity.User;
import com.mb.reddit.repository.PostRepository;
import com.mb.reddit.repository.PostVoteRepository;
import com.mb.reddit.service.PostVoteService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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
    public void addVoteByPostId(Long postId, Boolean isLike) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found " + postId));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentuser = userRepository.findUserByUsername(username);

        PostVote postVote = postVoteRepository.getPostVoteByUserIdAndPostId(postId, currentuser.getId()).orElse(new PostVote());

        postVote.setPost(post);
        postVote.setUser(currentuser);
        postVote.setIsLike(isLike);
        postVote.setLikedAt(LocalDateTime.now());

        postVoteRepository.save(postVote);
    }

    @Override
    public void removeVoteByPostId(Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentuser = userRepository.findUserByUsername(username);

        PostVote postVote = postVoteRepository.getPostVoteByUserIdAndPostId(postId, currentuser.getId()).orElseThrow(() -> new RuntimeException("No postVote present " + postId));

        postVoteRepository.delete(postVote);
    }
}
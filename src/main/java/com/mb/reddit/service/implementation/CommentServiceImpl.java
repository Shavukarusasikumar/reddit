package com.mb.reddit.service.implementation;

import com.mb.reddit.entity.Comment;
import com.mb.reddit.entity.CommentVote;
import com.mb.reddit.entity.Post;
import com.mb.reddit.entity.User;
import com.mb.reddit.repository.CommentRepository;
import com.mb.reddit.repository.CommentVoteRepository;
import com.mb.reddit.repository.PostRepository;
import com.mb.reddit.repository.UserRepository;
import com.mb.reddit.service.CommentService;
import org.hibernate.Hibernate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentVoteRepository commentVoteRepository;
    private final UserServiceImpl userServiceImpl;

    public CommentServiceImpl(CommentRepository commentRepository, PostRepository postRepository,
                              UserRepository userRepository, CommentVoteRepository commentVoteRepository,
                              UserServiceImpl userServiceImpl) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.commentVoteRepository = commentVoteRepository;
        this.userServiceImpl = userServiceImpl;
    }

    @Override
    @Transactional
    public Comment createComment(Comment comment, Long postId, Long parentCommentId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        User user = userServiceImpl.getLoggedInUser();

        comment.setPost(post);
        comment.setUser(user);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());

        if (parentCommentId != null) {
            Comment parentComment = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));
            comment.setParentComment(parentComment);
        }

        return commentRepository.save(comment);
    }

    @Override
    @Transactional
    public void deleteCommentById(Long id) {
        Optional<Comment> optionalComment = commentRepository.findById(id);

        if (optionalComment.isEmpty()) {
            throw new RuntimeException("Comment not found");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        if (!username.equals(optionalComment.get().getUser().getUsername())) {
            throw new RuntimeException("User not allowed to update comment");
        }

        commentRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Comment updateComment(Long commentId, String updatedContent) {
        Optional<Comment> optionalComment = commentRepository.findById(commentId);

        if (optionalComment.isEmpty()) {
            throw new RuntimeException("Comment not found");
        }

        Comment existingComment = optionalComment.get();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        if (!username.equals(existingComment.getUser().getUsername())) {
            throw new RuntimeException("User not allowed to update comment");
        }

        existingComment.setContent(updatedContent);
        existingComment.setUpdatedAt(LocalDateTime.now());

        return commentRepository.save(existingComment);
    }

    @Override
    public List<CommentVote> getCommentVotesByCommentId(Long commentId) {
        return commentVoteRepository.getCommentVotesByCommentId(commentId);
    }

    @Override
    public List<Comment> getRepliesByCommentId(Long commentId) {
        return commentRepository.findByParentCommentId(commentId);
    }

    @Override
    public Comment getCommentById(Long commentId) {
        Optional<Comment> optionalComment = commentRepository.findById(commentId);

        if (optionalComment.isEmpty()) {
            throw new RuntimeException("Comment not found");
        }

        return optionalComment.get();
    }

    @Override
    public List<Comment> getTopLevelComments(Long postId) {
        List<Comment> topLevelComments = commentRepository.findByPostIdAndParentCommentIsNull(postId);
        return loadNestedReplies(topLevelComments);
    }

    private List<Comment> loadNestedReplies(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            return comments;
        }

        for (Comment comment : comments) {
            if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
                Hibernate.initialize(comment.getReplies());

                loadNestedReplies(new ArrayList<>(comment.getReplies()));
            }
        }
        return comments;
    }

    @Override
    public int getVoteCountForComment(Long commentId) {
        List<CommentVote> votes = commentVoteRepository.getCommentVotesByCommentId(commentId);
        if (votes == null || votes.isEmpty()) {
            return 0;
        }

        return votes.stream()
                .mapToInt(vote -> Boolean.TRUE.equals(vote.getIsLike()) ? 1 : -1)
                .sum();
    }
}
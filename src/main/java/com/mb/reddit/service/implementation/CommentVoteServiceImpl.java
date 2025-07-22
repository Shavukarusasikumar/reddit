package com.mb.reddit.service.implementation;

import com.mb.reddit.entity.Comment;
import com.mb.reddit.entity.CommentVote;
import com.mb.reddit.entity.CustomUserDetails;
import com.mb.reddit.entity.User;
import com.mb.reddit.repository.CommentRepository;
import com.mb.reddit.repository.CommentVoteRepository;
import com.mb.reddit.repository.UserRepository;
import com.mb.reddit.service.CommentVoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CommentVoteServiceImpl implements CommentVoteService {

	private final CommentVoteRepository commentVoteRepository;
	private final UserRepository userRepository;
	private final CommentRepository commentRepository;
	private final UserServiceImpl userServiceImpl;

	public CommentVoteServiceImpl(CommentVoteRepository commentVoteRepository,
								  UserRepository userRepository,
								  CommentRepository commentRepository,
								  UserServiceImpl userServiceImpl) {
		this.commentVoteRepository = commentVoteRepository;
		this.userRepository = userRepository;
		this.commentRepository = commentRepository;
		this.userServiceImpl = userServiceImpl;
	}

	@Override
	public void addUpVoteByCommentId(Long commentId, Long userId) {
		handleVote(commentId, true, userId);
	}

	@Override
	@Transactional
	public void addDownVoteByCommentId(Long commentId, Long userId) {
		handleVote(commentId, false, userId);
	}

	private void handleVote(Long commentId, boolean isUpvote, Long userId) {
		Comment comment = commentRepository.findById(commentId)
				.orElseThrow(() -> new RuntimeException("Comment not found"));

		Optional<CommentVote> existingVote = commentVoteRepository.findByUserIdAndCommentId(userId, commentId);
        User user = userServiceImpl.getCurrentUser();

		if (existingVote.isPresent()) {
			CommentVote vote = existingVote.get();

			if (vote.getIsLike() == isUpvote) {
				commentVoteRepository.delete(vote);
				return;
			}

			vote.setIsLike(isUpvote);
			vote.setReactedAt(LocalDateTime.now());

			commentVoteRepository.save(vote);
		} else {
			CommentVote newVote = new CommentVote();
			newVote.setUser(user);
			newVote.setComment(comment);
			newVote.setIsLike(isUpvote);
			newVote.setReactedAt(LocalDateTime.now());

			commentVoteRepository.save(newVote);
		}
	}

	@Override
	@Transactional
	public void removeVoteByCommentId(Long commentId, Long userId) {
		commentVoteRepository.findByUserIdAndCommentId(userId, commentId)
				.ifPresent(commentVoteRepository::delete);
	}

	@Override
	public Boolean getVoteStatusByCommentId(Long commentId) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated() ||
				authentication.getPrincipal() instanceof String) {
			return false;
		}
		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

		return commentVoteRepository.findByUserIdAndCommentId(userDetails.getId(), commentId)
				.map(CommentVote::getIsLike)
				.orElse(null);
	}

	@Override
	public Long getPostIdForComment(Long commentId) {
		return commentRepository.findById(commentId)
				.map(comment -> comment.getPost().getId())
				.orElseThrow(() -> new RuntimeException("Comment not found"));
	}

	@Override
	public Boolean getVoteStatusByCommentIdAndCurrentUser(Long commentId) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated() ||
				authentication.getPrincipal() instanceof String) {
			return null;
		}
		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

		return commentVoteRepository.findByUserIdAndCommentId(userDetails.getId(), commentId)
				.map(CommentVote::getIsLike)
				.orElse(null);
	}
}
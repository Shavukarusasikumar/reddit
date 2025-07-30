package com.mb.reddit.service.implementation;

import com.mb.reddit.entity.Comment;
import com.mb.reddit.entity.CommentVote;
import com.mb.reddit.entity.CustomUserDetails;
import com.mb.reddit.entity.User;
import com.mb.reddit.exception.custom.CommentNotFoundException;
import com.mb.reddit.repository.CommentRepository;
import com.mb.reddit.repository.CommentVoteRepository;
import com.mb.reddit.service.CommentVoteService;

import com.mb.reddit.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CommentVoteServiceImpl implements CommentVoteService {

	private final CommentVoteRepository commentVoteRepository;
	private final CommentRepository commentRepository;
	private final UserService userService;

	public CommentVoteServiceImpl(CommentVoteRepository commentVoteRepository,
								  CommentRepository commentRepository,
								  UserService userService) {
		this.commentVoteRepository = commentVoteRepository;
		this.commentRepository = commentRepository;
		this.userService = userService;
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
				.orElseThrow(() -> new CommentNotFoundException("Comment not found" + commentId));

		Optional<CommentVote> existingVote = commentVoteRepository.findByUserIdAndCommentId(userId, commentId);
        User user = userService.getCurrentUser();

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
}
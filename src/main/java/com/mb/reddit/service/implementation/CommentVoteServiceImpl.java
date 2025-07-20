package com.mb.reddit.service.implementation;

import com.mb.reddit.entity.Comment;
import com.mb.reddit.entity.CommentVote;
import com.mb.reddit.entity.User;
import com.mb.reddit.repository.CommentRepository;
import com.mb.reddit.repository.CommentVoteRepository;
import com.mb.reddit.repository.UserRepository;
import com.mb.reddit.service.CommentVoteService;
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

	public CommentVoteServiceImpl(CommentVoteRepository commentVoteRepository,
								  UserRepository userRepository,
								  CommentRepository commentRepository) {
		this.commentVoteRepository = commentVoteRepository;
		this.userRepository = userRepository;
		this.commentRepository = commentRepository;
	}

	@Override
	@Transactional
	public void addUpVoteByCommentId(Long commentId) {
		handleVote(commentId, true);
	}

	@Override
	@Transactional
	public void addDownVoteByCommentId(Long commentId) {
		handleVote(commentId, false);
	}

	private void handleVote(Long commentId, boolean isUpvote) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName();
		User user = userRepository.findUserByUsername(username);
		Comment comment = commentRepository.findById(commentId)
				.orElseThrow(() -> new RuntimeException("Comment not found"));

		Optional<CommentVote> existingVote = commentVoteRepository.findByUserIdAndCommentId(user.getId(), commentId);

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
	public void removeVoteByCommentId(Long commentId) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName();
		User user = userRepository.findUserByUsername(username);

		commentVoteRepository.findByUserIdAndCommentId(user.getId(), commentId)
				.ifPresent(commentVoteRepository::delete);
	}

	@Override
	public Boolean getVoteStatusByCommentId(Long commentId) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			return null;
		}

		String username = authentication.getName();
		User user = userRepository.findUserByUsername(username);

		return commentVoteRepository.findByUserIdAndCommentId(user.getId(), commentId)
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
	public Boolean getVoteStatusByCommentIdAndUsername(Long commentId, String username) {
		if (username == null) return null;

		User user = userRepository.findUserByUsername(username);
		if (user == null) return null;

		return commentVoteRepository.findByUserIdAndCommentId(user.getId(), commentId)
				.map(CommentVote::getIsLike)
				.orElse(null);
	}
}
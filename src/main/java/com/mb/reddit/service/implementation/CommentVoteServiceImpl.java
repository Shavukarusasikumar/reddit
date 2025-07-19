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

	public  CommentVoteServiceImpl(CommentVoteRepository commentVoteRepository, UserRepository userRepository,
								   CommentRepository commentRepository) {
		this.commentVoteRepository = commentVoteRepository;
		this.userRepository = userRepository;
		this.commentRepository = commentRepository;
	}

	@Override
	@Transactional
	public void addUpVoteByCommentId(Long commentId) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName();

		User user = userRepository.findUserByUsername(username);

		Comment comment = commentRepository.findById(commentId)
				.orElseThrow(() -> new RuntimeException("Comment not found"));

		Optional<CommentVote> optionalCommentVote = commentVoteRepository
				.findByUserIdAndCommentId(user.getId(), commentId);

		if (optionalCommentVote.isPresent()) {
			CommentVote existingVote = optionalCommentVote.get();
			existingVote.setIsLike(true);
			existingVote.setReactedAt(LocalDateTime.now());

			commentVoteRepository.save(existingVote);
		} else {
			CommentVote newVote = new CommentVote();
			newVote.setUser(user);
			newVote.setComment(comment);
			newVote.setIsLike(true);
			newVote.setReactedAt(LocalDateTime.now());

			commentVoteRepository.save(newVote);
		}
	}

	@Override
	@Transactional
	public void addDownVoteByCommentId(Long commentId) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName();

		User user = userRepository.findUserByUsername(username);

		Comment comment = commentRepository.findById(commentId)
				.orElseThrow(() -> new RuntimeException("Comment not found"));

		Optional<CommentVote> optionalCommentVote = commentVoteRepository
				.findByUserIdAndCommentId(user.getId(), commentId);

		if (optionalCommentVote.isPresent()) {
			CommentVote existingVote = optionalCommentVote.get();
			existingVote.setIsLike(false);
			existingVote.setReactedAt(LocalDateTime.now());

			commentVoteRepository.save(existingVote);
		} else {
			CommentVote newVote = new CommentVote();
			newVote.setUser(user);
			newVote.setComment(comment);
			newVote.setIsLike(false);
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

		CommentVote commentVote = commentVoteRepository
				.findByUserIdAndCommentId(user.getId(), commentId)
				.orElseThrow(() -> new RuntimeException("CommentVote not found"));

		if (!commentVote.getUser().getId().equals(user.getId())) {
			throw new RuntimeException("You are not allowed to remove this vote");
		}

		commentVoteRepository.delete(commentVote);
	}

	@Override
	public Boolean getVoteStatusByCommentId(Long commentId) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName();

		User user = userRepository.findUserByUsername(username);

		Optional<CommentVote> optionalVote = commentVoteRepository
				.findByUserIdAndCommentId(user.getId(), commentId);

		return optionalVote.map(CommentVote::getIsLike).orElse(null);
	}
}
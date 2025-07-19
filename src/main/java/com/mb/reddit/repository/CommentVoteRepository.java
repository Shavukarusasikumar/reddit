package com.mb.reddit.repository;

import com.mb.reddit.entity.CommentVote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentVoteRepository extends JpaRepository<CommentVote, Long> {
	List<CommentVote> getCommentVotesByCommentId(Long commentId);
	Optional<CommentVote> findByUserIdAndCommentId(Long userId, Long commentId);
}
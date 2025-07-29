package com.mb.reddit.repository;

import com.mb.reddit.entity.CommentVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentVoteRepository extends JpaRepository<CommentVote, Long> {

	@Query("SELECT cv FROM CommentVote cv WHERE cv.comment.id = :commentId")
	List<CommentVote> getCommentVotesByCommentId(@Param("commentId") Long commentId);

	@Query("SELECT cv FROM CommentVote cv WHERE cv.user.id = :userId AND cv.comment.id = :commentId")
	Optional<CommentVote> findByUserIdAndCommentId(@Param("userId") Long userId, @Param("commentId") Long commentId);
}
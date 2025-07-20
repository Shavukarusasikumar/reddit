package com.mb.reddit.repository;

import com.mb.reddit.entity.Comment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
	List<Comment> findByParentCommentId(Long parentCommentId);

	@Query("SELECT c FROM Comment c WHERE c.post.id = :postId")
	List<Comment> getCommentsByPostId(@Param("postId") Long postId);

	@Query("SELECT c FROM Comment c LEFT JOIN FETCH c.replies WHERE c.post.id = :postId AND c.parentComment IS NULL")
	List<Comment> getTopLevelCommentsByPostId(@Param("postId") Long postId);

	@EntityGraph(attributePaths = {"replies", "replies.replies"})
	List<Comment> findByPostIdAndParentCommentIsNull(Long postId);
}

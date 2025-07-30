package com.mb.reddit.repository;

import com.mb.reddit.entity.Comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

	@Query("SELECT c FROM Comment c LEFT JOIN FETCH c.user WHERE c.post.id = :postId")
	List<Comment> findAllByPostIdWithUsers(@Param("postId") Long postId);
}

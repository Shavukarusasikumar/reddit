package com.mb.reddit.repository;

import com.mb.reddit.entity.Comment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
	@EntityGraph(attributePaths = {"replies", "replies.replies"})
	List<Comment> findByPostIdAndParentCommentIsNull(Long postId);
}

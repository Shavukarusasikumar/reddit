package com.mb.reddit.repository;

import com.mb.reddit.entity.Comment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

	List<Comment> findByPostIdAndParentCommentIsNull(Long postId);
}

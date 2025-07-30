package com.mb.reddit.service;

import com.mb.reddit.entity.Comment;

import java.util.List;

public interface CommentService {

	Comment createComment(Comment comment,Long postId, Long parentCommentId);
	void deleteCommentById(Long id);
	Comment updateComment(Long commentId, String updatedContent);
	Comment getCommentById(Long id);
	List<Comment> getTopLevelComments(Long postId);
	int getVoteCountForComment(Long commentId);
}
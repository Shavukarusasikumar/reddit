package com.mb.reddit.service;

import java.util.List;

public interface CommentService {

	Comment createComment(String content, Long userId, Long postId, Long parentCommentId);
	void deleteCommentById(Long id);
	Comment updateComment(Long commentId, Long userId, String updatedContent);
	List<CommentVote> getCommentVotesByCommentId(Long commentId);
	List<Comment> getRepliesByCommentId(Long commentId);
	Comment getCommentById(Long id);
}
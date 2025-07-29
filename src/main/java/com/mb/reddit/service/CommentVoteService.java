package com.mb.reddit.service;

public interface CommentVoteService {
	void addUpVoteByCommentId(Long commentId, Long userId);
	void addDownVoteByCommentId(Long commentId, Long userId);
	void removeVoteByCommentId(Long commentId, Long userId);
	Boolean getVoteStatusByCommentId(Long commentId);
	Long getPostIdForComment(Long commentId);
	Boolean getVoteStatusByCommentIdAndCurrentUser(Long commentId);
}
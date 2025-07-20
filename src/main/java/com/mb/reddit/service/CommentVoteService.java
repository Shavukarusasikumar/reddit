package com.mb.reddit.service;

public interface CommentVoteService {
	void addUpVoteByCommentId(Long commentId);
	void addDownVoteByCommentId(Long commentId);
	void removeVoteByCommentId(Long commentId);
	Boolean getVoteStatusByCommentId(Long commentId);
	Long getPostIdForComment(Long commentId);
	Boolean getVoteStatusByCommentIdAndUsername(Long commentId, String username);
}
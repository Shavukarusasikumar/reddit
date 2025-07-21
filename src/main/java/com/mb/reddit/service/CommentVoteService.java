package com.mb.reddit.service;

import com.mb.reddit.entity.User;

public interface CommentVoteService {
	void addUpVoteByCommentId(Long commentId, User user);
	void addDownVoteByCommentId(Long commentId, User user);
	void removeVoteByCommentId(Long commentId, User user);
	Boolean getVoteStatusByCommentId(Long commentId);
	Long getPostIdForComment(Long commentId);
	Boolean getVoteStatusByCommentIdAndUsername(Long commentId, String username);
}
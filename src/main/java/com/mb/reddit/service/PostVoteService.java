package com.mb.reddit.service;

public interface PostVoteService {

    void addVoteByPostId(Long postId, Boolean isLike);
    void removeVoteByPostId(Long postId);
    Boolean getVoteStatusByPostId(Long postId);
    Integer getPostVotesByPostId(Long postId);
}
package com.mb.reddit.service;

import com.mb.reddit.entity.Comment;
import com.mb.reddit.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface PostService {

    Post getPostById(Long postId);
    Post createPost(Post post, Long communityId,MultipartFile media);
    Post updatePost(Post post);
    void deletePost(Long postId);

    Page<Post> getAllPost(int pageNumber, int pageSize, String sortBy);
    Page<Post> getPostsByCommunityId(Long communityId, int pageNumber, int pageSize);

    List<Comment> getCommentsByPostId(Long postId);
    Integer getPostVotesByPostId(Long postId);

    Page<Post> getPostsByUserId(Long userId, int page, int size);

    Page<Post> getUpvotedPostsByUserId(Long userId, int page, int size);

    Page<Post> getDownVotedPostsByUserId(Long userId, int page, int size);

    Page<Post> getSavedPostsByUserId(Long userId, int page, int size);
}
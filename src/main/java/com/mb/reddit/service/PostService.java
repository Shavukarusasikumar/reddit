package com.mb.reddit.service;

import com.mb.reddit.dto.PostWithVotesDTO;
import com.mb.reddit.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.web.multipart.MultipartFile;


public interface PostService {

    Post getPostById(Long postId);
    Post createPost(Post post, Long communityId, MultipartFile media);
    Post updatePost(PostWithVotesDTO post, MultipartFile media,boolean removeMedia);
    void deletePost(Long postId);
    Page<PostWithVotesDTO> getAllPost(int pageNumber, int pageSize, String sort, String time, String keyword);
    Page<Post> getPostsByCommunityId(Long communityId, int pageNumber, int pageSize);
    Integer getPostVotesByPostId(Long postId);
    Page<PostWithVotesDTO> getPostsByUserId(Long userId, int page, int size);
    Page<PostWithVotesDTO> getUpvotedPostsByUserId(Long userId, int page, int size);
    Page<PostWithVotesDTO> getDownVotedPostsByUserId(Long userId, int page, int size);
    PostWithVotesDTO getPostWithVotesByPostId(@Param("postId") Long postId);
    Page<PostWithVotesDTO> getSavedPostsByUserId(Long userId, int page, int size);
}
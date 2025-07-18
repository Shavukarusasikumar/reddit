package com.mb.reddit.service.implementation;

import com.mb.reddit.entity.Comment;
import com.mb.reddit.entity.Post;
import com.mb.reddit.repository.CommentRepository;
import com.mb.reddit.repository.PostRepository;
import com.mb.reddit.repository.PostVoteRepository;
import com.mb.reddit.service.PostService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final CloudinaryService cloudinaryService;
    private final PostVoteRepository postVoteRepository;

    public PostServiceImpl(PostRepository postRepository, CommentRepository commentRepository,
                           CloudinaryService cloudinaryService, PostVoteRepository postVoteRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.cloudinaryService = cloudinaryService;
        this.postVoteRepository = postVoteRepository;
    }

    @Override
    public Post getPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(()-> new RuntimeException("Post not found with id " + postId));
    }

    @Override
    public Page<Post> getAllPost(int pageNumber, int pageSize, String sortby) {
        Sort sort = Sort.by(sortby).descending(); //TODO check sort functionality
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        return postRepository.findAll(pageable);
    }

    @Override
    public List<Comment> getCommentsByPostId(Long postId) {
        return commentRepository.getCommentsByPostId(postId);
    }

    @Override
    @Transactional
    public void deletePost(Long postId) {
        postRepository.deleteById(postId);
    }

    @Transactional
    @Override
    public Post createPost(Post post, MultipartFile media) {
        if(media != null){
            try {
                String mediaUrl = cloudinaryService.uploadFile(media);
                post.setMediaUrl(mediaUrl);
            } catch (IOException exception) {
                throw new RuntimeException("Failed to upload media", exception);
            }
        }

        post.setCreatedAt(LocalDateTime.now());
        //TODO : Have to add community and user

        return postRepository.save(post);
    }

    @Override
    @Transactional
    public Post updatePost(Post updatedPost) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Post oldPost = postRepository.findById(updatedPost.getId()).orElseThrow();

        if(!username.equals(oldPost.getAuthor().getUsername())){
            throw new RuntimeException("UnAuthorized");
        }

        updatedPost.setCreatedAt(oldPost.getCreatedAt());
        updatedPost.setUpdatedAt(LocalDateTime.now());

        return postRepository.save(updatedPost);
    }

    @Override
    public Integer getPostVotesByPostId(Long postId) {
        Integer upVoteCount = postVoteRepository.countUpvoteByPostId(postId);
        Integer downVoteCount = postVoteRepository.countDownvoteByPostId(postId);

        return upVoteCount-downVoteCount;
    }

    @Override
    public Page<Post> getPostsByCommunityId(Long communityId, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return postRepository.getPostsByCommunityId(communityId, pageable);
    }
}
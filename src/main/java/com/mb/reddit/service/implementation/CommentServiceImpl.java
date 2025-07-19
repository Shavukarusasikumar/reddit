package com.mb.reddit.service.implementation;

import com.mb.reddit.entity.Comment;
import com.mb.reddit.entity.CommentVote;
import com.mb.reddit.entity.Post;
import com.mb.reddit.entity.User;
import com.mb.reddit.repository.CommentRepository;
import com.mb.reddit.repository.CommentVoteRepository;
import com.mb.reddit.repository.PostRepository;
import com.mb.reddit.repository.UserRepository;
import com.mb.reddit.service.CommentService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CommentServiceImpl implements CommentService {

	private final CommentRepository commentRepository;
	private final PostRepository postRepository;
	private final UserRepository userRepository;
	private final CommentVoteRepository commentVoteRepository;

	public CommentServiceImpl(CommentRepository commentRepository, PostRepository postRepository,
							  UserRepository userRepository, CommentVoteRepository commentVoteRepository) {
		this.commentRepository = commentRepository;
		this.postRepository = postRepository;
		this.userRepository = userRepository;
		this.commentVoteRepository = commentVoteRepository;
	}

	@Override
	@Transactional
	public Comment createComment(String content, Long userId, Long postId, Long parentCommentId) {
		Optional<Post> optionalPost = postRepository.findById(postId);

		if (optionalPost.isEmpty()){
			throw  new RuntimeException("Post not  found");
		}

		Optional<User> optionalUser = userRepository.findById(userId);

		if (optionalUser.isEmpty()){
			throw  new RuntimeException("User not  found");
		}

		Comment comment = new Comment();
		comment.setContent(content);
		comment.setPost(optionalPost.get());
		comment.setUser(optionalUser.get());
		comment.setCreatedAt(LocalDateTime.now());
		comment.setUpdatedAt(LocalDateTime.now());

		if(parentCommentId != null){
			Optional<Comment> parentComment = commentRepository.findById(parentCommentId);

			if (parentComment.isEmpty()){
				throw  new RuntimeException("Parent comment not found");
			}

			comment.setParentComment(parentComment.get());
		}

		return commentRepository.save(comment);
	}

	@Override
	@Transactional
	public void deleteCommentById(Long id) {
		Optional<Comment> optionalComment = commentRepository.findById(id);

		if (optionalComment.isEmpty()){
			throw  new RuntimeException("Comment not found");
		}

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName();

		if(!username.equals(optionalComment.get().getUser().getUsername())){
			throw  new RuntimeException("User not allowed to update comment");
		}

		commentRepository.deleteById(id);
	}

	@Override
	@Transactional
	public Comment updateComment(Long commentId, Long userId, String updatedContent) {
		Optional<Comment> optionalComment = commentRepository.findById(commentId);

		if (optionalComment.isEmpty()){
			throw  new RuntimeException("Comment not found");
		}

		Comment existingComment = optionalComment.get();

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName();

		if(!username.equals(existingComment.getUser().getUsername())){
			throw  new RuntimeException("User not allowed to update comment");
		}

		existingComment.setContent(updatedContent);
		existingComment.setUpdatedAt(LocalDateTime.now());

		return commentRepository.save(existingComment);
	}

	@Override
	public List<CommentVote> getCommentVotesByCommentId(Long commentId) {
		return commentVoteRepository.getCommentVotesByCommentId(commentId);
	}

	@Override
	public List<Comment> getRepliesByCommentId(Long commentId) {
		return commentRepository.findByParentCommentId(commentId);
	}

	@Override
	public Comment getCommentById(Long commentId) {
		Optional<Comment> optionalComment = commentRepository.findById(commentId);

		if (optionalComment.isEmpty()){
			throw  new RuntimeException("Comment not found");
		}

		return optionalComment.get();
	}
}

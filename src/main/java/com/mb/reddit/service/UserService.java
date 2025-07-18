package com.mb.reddit.service;

import com.mb.reddit.entity.Comment;
import com.mb.reddit.entity.Community;
import com.mb.reddit.entity.Post;
import com.mb.reddit.entity.User;

import org.springframework.data.domain.Page;

import java.util.List;

public interface UserService {

    User addUser(User user);
    List<User> getAllUsers();
    Page<Post> getAllPostsByUserId(long userId, int pageNo, int pageSize);
    Page<Post> getSavedPostsByUserId(long userId, int pageNo, int pageSize);
    List<Comment> getCommentsByUserId(long userId);
    List<User> getFollowersByUserId(long userId);
    List<User> getFollowingByUserId(long userId);
    Page<Post> getUpVotedPostsByUserId(long userId);
    Page<Post> getDownVotedPostsByUserId(long userId);
    List<Community> getJoinedCommunitiesByUserId(long userId);
    List<Community> getCreatedCommunitiesByUserId(long userId);
    User getUserById(long userId);
    void addFollowingById(long id);
    void addFollowerById(long id);
    void deleteFollowerById(long id);
    void deleteFollowingById(long id);
}

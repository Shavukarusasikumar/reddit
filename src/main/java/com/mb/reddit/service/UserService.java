package com.mb.reddit.service;

import com.mb.reddit.dto.UserKarmaDTO;
import com.mb.reddit.entity.Comment;
import com.mb.reddit.entity.Community;
import com.mb.reddit.entity.Post;
import com.mb.reddit.entity.User;

import org.springframework.data.domain.Page;

import java.util.List;

public interface UserService {

    List<User> getAllUsers();
    User getUserById(long userId);
    User getCurrentUser();
    User registerUser(String username, String email, String password, String bio);
    void addPostToUserSavedPosts(Long postId, Long userId);
    void removePostFromUserSavedPosts(Long postId, Long userId);
    boolean isPostSavedByUser(Long postId, Long userId);
    UserKarmaDTO getKarmaDto(Long userId);
}

package com.mb.reddit.service.implementation;

import com.mb.reddit.dto.UserKarmaDTO;
import com.mb.reddit.entity.*;
import com.mb.reddit.exception.custom.PostNotFoundException;
import com.mb.reddit.exception.custom.UserNotFoundException;
import com.mb.reddit.repository.PostRepository;
import com.mb.reddit.repository.UserRepository;
import com.mb.reddit.service.UserService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PostRepository postRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal() instanceof String) {
            return null;
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new UserNotFoundException("No User Found With Id: " + userDetails.getId()));
    }

    @Override
    @Transactional
    public User registerUser(String username, String email, String password, String bio) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setBio(bio != null ? bio : "");

        return userRepository.save(user);
    }

    @Override
    public void addPostToUserSavedPosts(Long postId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found"));

        if (!user.getSavedPosts().contains(post)) {
            user.getSavedPosts().add(post);

            userRepository.save(user);
        }
    }

    @Override
    public void removePostFromUserSavedPosts(Long postId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found"));

        user.getSavedPosts().remove(post);

        userRepository.save(user);
    }

    @Override
    public boolean isPostSavedByUser(Long postId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return user.getSavedPosts().stream()
                .anyMatch(p -> p.getId().equals(postId));
    }

    @Override
    public UserKarmaDTO getKarmaDto(Long userId) {
        UserKarmaDTO userKarmaDTO = userRepository.getKarmaByUserId(userId);

        return userKarmaDTO;
    }
}

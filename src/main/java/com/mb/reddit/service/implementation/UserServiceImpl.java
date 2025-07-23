package com.mb.reddit.service.implementation;

import com.mb.reddit.dto.UserKarmaDTO;
import com.mb.reddit.entity.*;
import com.mb.reddit.repository.CommunityRepository;
import com.mb.reddit.repository.PostRepository;
import com.mb.reddit.repository.UserRepository;
import com.mb.reddit.service.CommunityService;
import com.mb.reddit.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommunityRepository communityRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PostRepository postRepository, CommunityRepository communityRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.communityRepository = communityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    @Override
    public void addPostToUserSavedPosts(Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findUserByUsername(username);
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));

        if(!user.getSavedPosts().contains(post)) {
            user.getSavedPosts().add(post);
            userRepository.save(user);
        }
    }

    @Override
    public void adduserToCommunityByCommunityId(Long communityId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findUserByUsername(username);
        Community community = communityRepository.findById(communityId).orElse(null);

        if(!user.getJoinedCommunities().contains(community)) {
            user.getJoinedCommunities().add(community);
            userRepository.save(user);
        }
    }

    @Override
    @Transactional
    public void removeUserFromCommunityByCommunityId(Long communityId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new EntityNotFoundException("Community not found with id: " + communityId));

        if (user.getJoinedCommunities().remove(community)) {
            userRepository.save(user); // Only save if a removal actually happened
        }
    }


    @Override
    public User addUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Page<Post> getAllPostsByUserId(long userId, int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        return userRepository.findAllPostsByUserId(userId, pageable);
    }

    @Override
    public Page<Post> getSavedPostsByUserId(long userId, int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        return userRepository.findAllSavedPostsByUserId(userId, pageable);
    }

    @Override
    public List<Comment> getCommentsByUserId(long userId) {
        return userRepository.findAllCommentsByUserId(userId);
    }

    @Override
    public List<User> getFollowersByUserId(long userId) {
        return userRepository.findAllFollowersByUserId(userId);
    }

    @Override
    public List<User> getFollowingByUserId(long userId) {
        return userRepository.findAllFollowingByUserId(userId);
    }

    @Override
    public Page<Post> getUpVotedPostsByUserId(long userId) {
        return null;
    }

    @Override
    public Page<Post> getDownVotedPostsByUserId(long userId) {
        return null;
    }

    @Override
    public List<Community> getJoinedCommunitiesByUserId(long userId) {
        return userRepository.findAllJoinedCommunitiesByUserId(userId);
    }

    @Override
    public List<Community> getCreatedCommunitiesByUserId(long userId) {
        return userRepository.findAllCreatedCommunitiesByUserId(userId);
    }

    @Override
    public User getUserById(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }

    @Override
    @Transactional
    public void addFollowingById(long id) {
        User currUser = getCurrentUser();

        List<User> following = userRepository.findAllFollowingByUserId(currUser.getId());
        User newUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        following.add(newUser);

        currUser.setFollowing(following);

        userRepository.save(currUser);
    }

    @Override
    @Transactional
    public void addFollowerById(long id) {
        User currUser = getCurrentUser();

        List<User> followers = userRepository.findAllFollowersByUserId(currUser.getId());
        User newUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id : " + id));

        followers.add(newUser);

        currUser.setFollowers(followers);

        userRepository.save(currUser);
    }

    @Override
    @Transactional
    public void deleteFollowerById(long id) {
        User currUser = getCurrentUser();

        List<User> followers = userRepository.findAllFollowersByUserId(currUser.getId());
        User newUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id " + id));

        for (int i = 0; i < followers.size(); i++) {
            if (Objects.equals(followers.get(i).getId(), newUser.getId())) {
                followers.remove(i);

                break;
            }
        }

        currUser.setFollowers(followers);

        userRepository.save(currUser);
    }

    @Override
    @Transactional
    public void deleteFollowingById(long id) {
        User currUser = getCurrentUser();

        List<User> following = userRepository.findAllFollowingByUserId(currUser.getId());
        User newUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id " + id));

        for (int i = 0; i < following.size(); i++) {

            if (Objects.equals(following.get(i).getId(), newUser.getId())) {
                following.remove(i);
                break;
            }
        }

        currUser.setFollowing(following);

        userRepository.save(currUser);
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal() instanceof String) {
            return null;
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("No User Found With Id: " + userDetails.getId()));
    }


    @Override
    public boolean hasUserJoinedCommunity(Community community) {

        User user = getCurrentUser();
        List<Community> joinedCommunities = user.getJoinedCommunities();

        for(Community joinedCommunity : joinedCommunities) {
            if(Objects.equals(joinedCommunity, community)) {
                return true;
            }
        }
        return false;
    }

    public User getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal() instanceof String) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof org.springframework.security.core.userdetails.User userDetails) {
            return userRepository.findUserByUsername(userDetails.getUsername());
        } else if (principal instanceof OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            return userRepository.findUserByEmail(email).orElse(null);
        } else if (principal instanceof String username) {
            return userRepository.findUserByUsername(username);
        }

        return null;
    }

    @Override
    public boolean isUsernameTaken(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean isEmailRegistered(String email) {
        return userRepository.existsByEmail(email);
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
                .orElseThrow(() -> new RuntimeException("User not found"));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!user.getSavedPosts().contains(post)) {
            user.getSavedPosts().add(post);
            userRepository.save(user);
        }
    }

    @Override
    public void removePostFromUserSavedPosts(Long postId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        user.getSavedPosts().remove(post);
        userRepository.save(user);
    }

    @Override
    public boolean isPostSavedByUser(Long postId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getSavedPosts().stream()
                .anyMatch(p -> p.getId().equals(postId));
    }

    @Override
    public UserKarmaDTO getKarmaDto(Long userId) {
        UserKarmaDTO userKarmaDTO = userRepository.getKarmaByUserId(userId);
        return userKarmaDTO;
    }
}

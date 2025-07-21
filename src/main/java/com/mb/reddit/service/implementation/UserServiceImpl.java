package com.mb.reddit.service.implementation;

import com.mb.reddit.entity.Comment;
import com.mb.reddit.entity.Community;
import com.mb.reddit.entity.Post;
import com.mb.reddit.entity.User;
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

    public UserServiceImpl(UserRepository userRepository, PostRepository postRepository, CommunityRepository communityRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.communityRepository = communityRepository;
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
        return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }

    @Override
    @Transactional
    public void addFollowingById(long id) {
        User currUser = getCurrentUser();

        List<User> following = userRepository.findAllFollowingByUserId(currUser.getId());
        User newUser = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        following.add(newUser);

        currUser.setFollowing(following);

        userRepository.save(currUser);
    }

    @Override
    @Transactional
    public void addFollowerById(long id) {
        User currUser = getCurrentUser();

        List<User> followers = userRepository.findAllFollowersByUserId(currUser.getId());
        User newUser = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found with id : " + id));

        followers.add(newUser);

        currUser.setFollowers(followers);

        userRepository.save(currUser);
    }

    @Override
    @Transactional
    public void deleteFollowerById(long id) {
        User currUser = getCurrentUser();

        List<User> followers = userRepository.findAllFollowersByUserId(currUser.getId());
        User newUser = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found with id " + id));

        for(int i = 0; i < followers.size(); i++) {
            if(Objects.equals(followers.get(i).getId(), newUser.getId())) {
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
        User newUser = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found with id " + id));

        for(int i = 0; i < following.size(); i++) {

            if(Objects.equals(following.get(i).getId(), newUser.getId())) {
                following.remove(i);
                break;
            }
        }

        currUser.setFollowing(following);

        userRepository.save(currUser);
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return userRepository.findUserByUsername(authentication.getName());
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

}

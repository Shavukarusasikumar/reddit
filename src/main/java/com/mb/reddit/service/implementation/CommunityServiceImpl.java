package com.mb.reddit.service.implementation;

import com.mb.reddit.entity.Community;
import com.mb.reddit.entity.User;
import com.mb.reddit.repository.CommunityRepository;
import com.mb.reddit.repository.UserRepository;
import com.mb.reddit.service.CommunityService;
import com.mb.reddit.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CommunityServiceImpl implements CommunityService {

    public final CommunityRepository communityRepository;
    public final CloudinaryService cloudinaryService;
    public final UserRepository userRepository;
    public final UserServiceImpl userService;

    public CommunityServiceImpl(CommunityRepository communityRepository, CloudinaryService cloudinaryService, UserRepository userRepository, UserServiceImpl userService) {
        this.userRepository = userRepository;
        this.communityRepository = communityRepository;
        this.userService = userService;
        this.cloudinaryService = cloudinaryService;
    }

    @Transactional
    @Override
    public Community createCommunity(Community community, MultipartFile fileIcon, MultipartFile fileBanner) {
        User user = userService.getCurrentUser();
        community.setCreator(user);
        community.setCreatedAt(LocalDateTime.now());
        if(fileIcon != null) {
            try {
                String iconUrl = cloudinaryService.uploadFile(fileIcon);
                community.setIconUrl(iconUrl);
            } catch(IOException exception) {
                throw new RuntimeException("Failed to upload media", exception);
            }
        }

        if(fileBanner != null) {
            try {
                String bannerUrl = cloudinaryService.uploadFile(fileBanner);
                community.setIconUrl(bannerUrl);
            } catch(IOException exception) {
                throw new RuntimeException("Failed to upload media", exception);
            }
        }

        user.getCreatedCommunities().add(community);
        return communityRepository.save(community);
    }

    @Override
    public void deleteCommunity(Long communityId) {
        communityRepository.deleteById(communityId);
    }

    @Override
    public List<User> getCommunityMembers(Long communityId) {
        return communityRepository.findById(communityId).orElseThrow(() -> new RuntimeException("Community Not found")).getMembers();
    }

    @Override
    public Long getMembersCountByCommunityId(Long communityId) {
        return (long) communityRepository.findById(communityId).orElseThrow(() -> new RuntimeException("Community Not found")).getMembers().size();
    }

    @Override
    public void addMemberByCommunityId(User member, Long communityId) {
        Community community = communityRepository.findById(communityId).orElseThrow(() -> new RuntimeException("Community Not found"));

        community.getMembers().add(member);

        communityRepository.save(community);
    }

    @Override
    public User getCreatorByCommunityId(Long communityId) {
        return communityRepository.findById(communityId).orElseThrow(() -> new RuntimeException("Community Not found")).getCreator();
    }

    @Override
    public Community getCommunityById(Long communityId) {
        return communityRepository.findById(communityId).orElseThrow(() -> new RuntimeException("Community Not found"));
    }

    @Override
    public List<Community> getAllCommunities() {
        return communityRepository.findAll();
    }

    @Override
    public List<Community> findUserJoinedCommunities() {

        User user = userService.getLoggedInUser();

        return communityRepository.findUserCommunities(user.getUsername());
    }

    @Override
    public List<Community> findCommunitiesUserCanPost() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        List<Community> result = new ArrayList<>(communityRepository.findPublicCommunities());
        result.addAll(communityRepository.findUserCommunities(username));

        return result.stream().distinct().toList();
    }
}
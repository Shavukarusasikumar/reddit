package com.mb.reddit.service;


import com.mb.reddit.entity.Community;
import com.mb.reddit.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CommunityService {

    Community createCommunity(Community community, MultipartFile fileIcon, MultipartFile fileBanner);

    void deleteCommunity(Long communityId);

    List<User> getCommunityMembers(Long communityId);

    Long getMembersCountByCommunityId(Long communityId);

    void addMemberByCommunityId(User member, Long communityId);

    User getCreatorByCommunityId(Long communityId);

    Community getCommunityById(Long communityId);

    List<Community> getAllCommunities();

    List<Community> findCommunitiesUserCanPost();
}
package com.mb.reddit.service;


import com.mb.reddit.dto.CommunityBasicDTO;
import com.mb.reddit.entity.Community;
import com.mb.reddit.entity.User;
import org.springframework.security.core.Authentication;
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

    Community getCommunityByName(String communityName);

    List<Community> getAllCommunities();

    List<CommunityBasicDTO> findCommunitiesUserCanPost();

    List<Community> findUserJoinedCommunities();

    void removeMemberByCommunityId(User member, Long communityId);



    List<Community> getCommunitiesByTopicId(Long topicId);
}
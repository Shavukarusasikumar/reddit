package com.mb.reddit.service.implementation;

import com.mb.reddit.entity.Community;
import com.mb.reddit.entity.User;
import com.mb.reddit.repository.CommunityRepository;
import com.mb.reddit.service.CommunityService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommunityServiceImpl implements CommunityService {

    public final CommunityRepository communityRepository;

    public CommunityServiceImpl(CommunityRepository communityRepository) {
        this.communityRepository = communityRepository;
    }

    @Override
    public Community createCommunity(Community community) {
        return communityRepository.save(community);
    }

    @Override
    public void deleteCommunity(Long communityId) {
        communityRepository.deleteById(communityId);
    }

    @Override
    public List<User> getCommunityMembers(Long communityId) {
        return communityRepository.findById(communityId).orElseThrow(() ->
                new RuntimeException("Community Not found")).getMembers();
    }

    @Override
    public Long getMembersCountByCommunityId(Long communityId) {
        return (long) communityRepository.findById(communityId).orElseThrow(() ->
                new RuntimeException("Community Not found")).getMembers().size();
    }

    @Override
    public void addMemberByCommunityId(User member, Long communityId) {
        Community community = communityRepository.findById(communityId).orElseThrow(() ->
                new RuntimeException("Community Not found"));

        community.getMembers().add(member);
        member.getJoinedCommunities().add(community);

        communityRepository.save(community);
    }

    @Override
    public User getCreatorByCommunityId(Long communityId) {
        return communityRepository.findById(communityId).orElseThrow(() ->
                new RuntimeException("Community Not found")).getCreator();
    }

    @Override
    public Community getCommunityById(Long communityId) {
        return communityRepository.findById(communityId).orElseThrow(() ->
                new RuntimeException("Community Not found"));
    }

    @Override
    public List<Community> getAllCommunities() {
        return communityRepository.findAll();
    }
}
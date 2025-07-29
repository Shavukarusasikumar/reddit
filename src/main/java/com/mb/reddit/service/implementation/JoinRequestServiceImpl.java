package com.mb.reddit.service.implementation;

import com.mb.reddit.entity.Community;
import com.mb.reddit.entity.JoinRequest;
import com.mb.reddit.entity.User;
import com.mb.reddit.repository.CommunityRepository;
import com.mb.reddit.repository.JoinRequestRepository;
import com.mb.reddit.service.JoinRequestService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class JoinRequestServiceImpl implements JoinRequestService {

    private final JoinRequestRepository joinRequestRepository;
    private final CommunityRepository communityRepository;


    public JoinRequestServiceImpl(JoinRequestRepository joinRequestRepository,
                                  CommunityRepository communityRepository) {
        this.joinRequestRepository = joinRequestRepository;
        this.communityRepository = communityRepository;
    }

    @Override
    @Transactional
    public void sendJoinRequest(Community community, User user) {

        Optional<JoinRequest> existing =
                joinRequestRepository.findByCommunityAndRequester(community, user);

        if (existing.isPresent()) {
                return;
        }

        JoinRequest request = new JoinRequest();
        request.setCommunity(community);
        request.setRequester(user);
        request.setRequestedAt(LocalDateTime.now());
        request.setApproved(false);

        joinRequestRepository.save(request);
    }

    @Override
    public List<JoinRequest> getJoinCommunityRequest(Long communityId) {
        return joinRequestRepository.findAllByCommunityId(communityId);
    }

    @Override
    public Long acceptJoinRequest(Long requestId) {
        JoinRequest request = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        Community community = request.getCommunity();
        User user = request.getRequester();

        community.getMembers().add(user);
        user.getJoinedCommunities().add(community);

        communityRepository.save(community);
        joinRequestRepository.delete(request);

        return community.getId();
    }

    @Override
    public Long rejectJoinRequest(Long requestId) {
        JoinRequest request = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        Community community = request.getCommunity();
        joinRequestRepository.deleteById(requestId);

        return community.getId();
    }
}

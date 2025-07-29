package com.mb.reddit.service;

import com.mb.reddit.entity.Community;
import com.mb.reddit.entity.JoinRequest;
import com.mb.reddit.entity.User;

import java.util.List;

public interface JoinRequestService {
    
    void sendJoinRequest(Community community, User user);
    List<JoinRequest> getJoinCommunityRequest(Long id);
    Long acceptJoinRequest(Long requestId);
    Long rejectJoinRequest(Long requestId);
}

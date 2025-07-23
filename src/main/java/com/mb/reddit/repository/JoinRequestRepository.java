package com.mb.reddit.repository;


import com.mb.reddit.entity.JoinRequest;
import com.mb.reddit.entity.Community;
import com.mb.reddit.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long> {

    Optional<JoinRequest> findByCommunityAndRequester(Community community, User requester);

    List<JoinRequest> findAllByCommunityId(Long communityId);
}

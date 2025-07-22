package com.mb.reddit.repository;

import com.mb.reddit.entity.Community;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommunityRepository  extends JpaRepository<Community, Long> {

    @Query("SELECT c FROM Community c WHERE c.isPrivate = false")
    List<Community> findPublicCommunities();

    @Query("SELECT c FROM Community c JOIN c.members m WHERE m.username = :username")
    List<Community> findUserCommunities(@Param("username") String username);

    Community findCommunityByName(String communityName);
}
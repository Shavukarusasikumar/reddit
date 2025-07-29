package com.mb.reddit.repository;

import com.mb.reddit.dto.CommunityBasicDTO;
import com.mb.reddit.entity.Community;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommunityRepository  extends JpaRepository<Community, Long> {

    @Query("SELECT c FROM Community c JOIN c.members m WHERE m.id = :userId")
    List<Community> findUserCommunities(@Param("userId") Long userId);

    Community findCommunityByName(String communityName);

    @Query("SELECT DISTINCT c FROM Community c JOIN c.topics t WHERE t.id = :topicId AND c.isPrivate = false")
    List<Community> findByTopicsId(@Param("topicId") Long topicId);

    @Query("""
    SELECT new com.mb.reddit.dto.CommunityBasicDTO(c.id, c.name, c.iconUrl)
    FROM Community c
    WHERE c.isPrivate = false
        """)
    List<CommunityBasicDTO> findPublicCommunities();

    @Query("""
    SELECT new com.mb.reddit.dto.CommunityBasicDTO(c.id, c.name, c.iconUrl)
    FROM Community c
    JOIN c.members m
    WHERE m.id = :userId
        """)
    List<CommunityBasicDTO> findUserJoinedCommunities(@Param("userId") Long userId);
}
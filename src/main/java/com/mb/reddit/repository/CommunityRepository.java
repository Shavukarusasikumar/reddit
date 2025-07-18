package com.mb.reddit.repository;

import com.mb.reddit.entity.Community;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityRepository  extends JpaRepository<Community, Long> {
}
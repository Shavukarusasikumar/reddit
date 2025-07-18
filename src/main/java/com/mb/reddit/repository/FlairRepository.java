package com.mb.reddit.repository;

import com.mb.reddit.entity.Flair;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlairRepository extends JpaRepository<Flair, Long> {
}
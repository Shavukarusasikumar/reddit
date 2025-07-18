package com.mb.reddit.repository;

import com.mb.reddit.entity.PostVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostVoteRepository extends JpaRepository<PostVote, Long> {

    @Query("SELECT COUNT(p) FROM PostVote p WHERE p.post.id = :postId AND p.isLike = true")
    Integer countUpvoteByPostId(@Param("postId") Long postId);

    @Query("SELECT COUNT(p) FROM PostVote p WHERE p.post.id = :postId AND p.isLike = false")
    Integer countDownvoteByPostId(@Param("postId") Long postId);

    @Query("SELECT p FROM PostVote p WHERE p.user.id = :userId AND p.post.id = :postId")
    Optional<PostVote> getPostVoteByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);
}
package com.mb.reddit.repository;

import com.mb.reddit.entity.Post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p WHERE p.community.id = :communityId")
    Page<Post> getPostsByCommunityId(@Param("communityId") Long communityId, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.isPublished = true AND p.community.isPrivate = false")
    Page<Post> findAllPublicPublishedPosts(Pageable pageable);

    @Query("SELECT DISTINCT p from Post p Where p.isPublished = true AND p.author.id = :userId")
    Page<Post> getPostsByUserId(@Param("userId") Integer userId, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Post p JOIN PostVote v ON v.post = p " +
            "WHERE p.isPublished = true AND v.user.id = :userId AND v.isLike = :islike " +
            "ORDER BY p.createdAt DESC")
    Page<Post> getVotedPostByUserId(@Param("userId") Integer userId,@Param("islike") Boolean islike, Pageable pageable);

}
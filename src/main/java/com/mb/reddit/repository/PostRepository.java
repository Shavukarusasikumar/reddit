package com.mb.reddit.repository;

import com.mb.reddit.dto.PostWithVotesDTO;
import com.mb.reddit.entity.Post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p WHERE p.community.id = :communityId")
    Page<Post> getPostsByCommunityId(@Param("communityId") Long communityId, Pageable pageable);

    //    @Query("""
    //    SELECT new com.mb.reddit.dto.PostWithVotesDTO(
    //        p.id,
    //        p.title,
    //        p.content,
    //        p.mediaUrl,
    //        p.community.name,
    //        p.community.iconUrl,
    //        p.createdAt,
    //        SUM(CASE WHEN v.isLike = true THEN 1 ELSE 0 END),
    //        SUM(CASE WHEN v.isLike = false THEN 1 ELSE 0 END),
    //        COUNT(c)
    //    )
    //            FROM Post p
    //            LEFT JOIN p.postVotes v
    //            LEFT JOIN p.comments c
    //            WHERE p.isPublished = true AND p.community.isPrivate = false
    //            GROUP BY p.id, p.title, p.content, p.mediaUrl, p.community.name, p.community.iconUrl, p.createdAt
    //            ORDER BY p.createdAt DESC
    //        """)
    //    Page<PostWithVotesDTO> findAllPublicPublishedPosts(Pageable pageable);

    @Query("SELECT DISTINCT p from Post p Where p.isPublished = true AND p.author.id = :userId")
    Page<Post> getPostsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Post p JOIN PostVote v ON v.post = p " + "WHERE p.isPublished = true AND v.user.id = :userId AND v.isLike = :islike " + "ORDER BY p.createdAt DESC")
    Page<Post> getVotedPostByUserId(@Param("userId") Long userId, @Param("islike") Boolean islike, Pageable pageable);

    @Query("SELECT u.savedPosts FROM User u WHERE u.id = :userId")
    Page<Post> getSavedPostsByUserId(@Param("userId") Long userId, Pageable pageable);

    // for top
    @Query("""
            SELECT new com.mb.reddit.dto.PostWithVotesDTO(
                p.id, p.title, p.content, p.mediaUrl,
                p.community.name, p.community.iconUrl, p.createdAt,
                SUM(CASE WHEN v.isLike = true THEN 1 ELSE 0 END),
                SUM(CASE WHEN v.isLike = false THEN 1 ELSE 0 END),
                COUNT(c)
            )
            FROM Post p
            LEFT JOIN p.postVotes v
            LEFT JOIN p.comments c
            WHERE p.isPublished = true AND p.community.isPrivate = false
            GROUP BY p.id, p.title, p.content, p.mediaUrl, p.community.name, p.community.iconUrl, p.createdAt
            ORDER BY SUM(CASE WHEN v.isLike = true THEN 1 ELSE 0 END) - SUM(CASE WHEN v.isLike = false THEN 1 ELSE 0 END) DESC
            """)
    Page<PostWithVotesDTO> findTopPosts(Pageable pageable);

    //for raising
    @Query("""
            SELECT new com.mb.reddit.dto.PostWithVotesDTO(
                p.id, p.title, p.content, p.mediaUrl,
                p.community.name, p.community.iconUrl, p.createdAt,
                SUM(CASE WHEN v.isLike = true THEN 1 ELSE 0 END),
                SUM(CASE WHEN v.isLike = false THEN 1 ELSE 0 END),
                COUNT(c)
            )
            FROM Post p
            LEFT JOIN p.postVotes v
            LEFT JOIN p.comments c
            WHERE p.isPublished = true AND p.community.isPrivate = false
            AND p.createdAt >= :threshold
            GROUP BY p.id, p.title, p.content, p.mediaUrl, p.community.name, p.community.iconUrl, p.createdAt
            ORDER BY 
                (SUM(CASE WHEN v.isLike = true THEN 1 ELSE 0 END) - SUM(CASE WHEN v.isLike = false THEN 1 ELSE 0 END)) DESC,
                COUNT(c) DESC
            """)
    Page<PostWithVotesDTO> findRisingPosts(@Param("threshold") LocalDateTime threshold, Pageable pageable);

    // for new
    @Query("""
            SELECT new com.mb.reddit.dto.PostWithVotesDTO(
                p.id, p.title, p.content, p.mediaUrl,
                p.community.name, p.community.iconUrl, p.createdAt,
                SUM(CASE WHEN v.isLike = true THEN 1 ELSE 0 END),
                SUM(CASE WHEN v.isLike = false THEN 1 ELSE 0 END),
                COUNT(c)
            )
            FROM Post p
            LEFT JOIN p.postVotes v
            LEFT JOIN p.comments c
            WHERE p.isPublished = true AND p.community.isPrivate = false
            GROUP BY p.id, p.title, p.content, p.mediaUrl, p.community.name, p.community.iconUrl, p.createdAt
            ORDER BY p.createdAt DESC
            """)
    Page<PostWithVotesDTO> findNewPosts(Pageable pageable);

    @Query("""
            SELECT new com.mb.reddit.dto.PostWithVotesDTO(
                p.id,
                p.title,
                p.content,
                p.mediaUrl,
                p.community.name,
                p.community.iconUrl,
                p.createdAt,
                COALESCE(SUM(CASE WHEN v.isLike = true THEN 1 ELSE 0 END), 0),
                COALESCE(SUM(CASE WHEN v.isLike = false THEN 1 ELSE 0 END), 0),
                COUNT(c)
            )
                    FROM Post p
                    LEFT JOIN p.postVotes v
                    LEFT JOIN p.comments c
                    GROUP BY p.id, p.title, p.content, p.mediaUrl, p.community.name, p.community.iconUrl, p.createdAt
                    ORDER BY COALESCE(SUM(CASE WHEN v.isLike = true THEN 1 ELSE 0 END), 0) DESC, p.createdAt DESC
            """)
    Page<PostWithVotesDTO> findPopularPosts(Pageable pageable);


    @Query("""
            SELECT new com.mb.reddit.dto.PostWithVotesDTO(
                p.id,
                p.title,
                p.content,
                p.mediaUrl,
                p.community.name,
                p.community.iconUrl,
                p.createdAt,
                (SELECT COUNT(v) FROM PostVote v WHERE v.post = p AND v.isLike = true),
                (SELECT COUNT(v) FROM PostVote v WHERE v.post = p AND v.isLike = false),
                (SELECT COUNT(c) FROM Comment c WHERE c.post = p)
            )
            FROM Post p
            WHERE p.isPublished = true
              AND p.community.isPrivate = false
              AND (
                  :keyword IS NULL OR :keyword = '' OR
                  LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(p.author.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            ORDER BY p.createdAt DESC
            """)
    Page<PostWithVotesDTO> searchPostsByKeyword(@Param("keyword") String keyword, Pageable pageable);
}

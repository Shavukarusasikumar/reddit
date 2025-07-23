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

    @Query("""
    SELECT new com.mb.reddit.dto.PostWithVotesDTO(
        p.id,
        p.title,
        p.content,
        p.mediaUrl,
        p.community.name,
        p.community.iconUrl,
        p.createdAt,
        (SELECT COUNT(v.id) FROM PostVote v WHERE v.post.id = p.id AND v.isLike = true),
        (SELECT COUNT(v.id) FROM PostVote v WHERE v.post.id = p.id AND v.isLike = false),
        (SELECT COUNT(c.id) FROM Comment c WHERE c.post.id = p.id)
    )
    FROM Post p
    WHERE p.isPublished = true AND p.author.id = :userId
    ORDER BY p.createdAt DESC
""")
    Page<PostWithVotesDTO> getPostDTOsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("""
    SELECT new com.mb.reddit.dto.PostWithVotesDTO(
        p.id,
        p.title,
        p.content,
        p.mediaUrl,
        p.community.name,
        p.community.iconUrl,
        p.createdAt,
        (SELECT COUNT(v2.id) FROM PostVote v2 WHERE v2.post.id = p.id AND v2.isLike = true),
        (SELECT COUNT(v2.id) FROM PostVote v2 WHERE v2.post.id = p.id AND v2.isLike = false),
        (SELECT COUNT(c.id) FROM Comment c WHERE c.post.id = p.id)
    )
    FROM PostVote v
    JOIN v.post p
    WHERE p.isPublished = true AND v.user.id = :userId AND v.isLike = :isLike
    ORDER BY p.createdAt DESC
""")
    Page<PostWithVotesDTO> getVotedPostsDTO(@Param("userId") Long userId,
                                            @Param("isLike") Boolean isLike,
                                            Pageable pageable);

    @Query("""
    SELECT new com.mb.reddit.dto.PostWithVotesDTO(
        p.id,
        p.title,
        p.content,
        p.mediaUrl,
        p.community.name,
        p.community.iconUrl,
        p.createdAt,
        (SELECT COUNT(v.id) FROM PostVote v WHERE v.post.id = p.id AND v.isLike = true),
        (SELECT COUNT(v.id) FROM PostVote v WHERE v.post.id = p.id AND v.isLike = false),
        (SELECT COUNT(c.id) FROM Comment c WHERE c.post.id = p.id)
    )
    FROM User u
    JOIN u.savedPosts p
    WHERE u.id = :userId AND p.isPublished = true
    ORDER BY p.createdAt DESC
""")
    Page<PostWithVotesDTO> getSavedPostDTOsByUserId(@Param("userId") Long userId, Pageable pageable);

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
        p.id,
        p.title,
        p.content,
        p.mediaUrl,
        p.community.name,
        p.community.iconUrl,
        p.createdAt,
        (SELECT COALESCE(SUM(CASE WHEN v.isLike = true THEN 1 ELSE 0 END), 0)
         FROM PostVote v
         WHERE v.post.id = p.id),
        (SELECT COALESCE(SUM(CASE WHEN v.isLike = false THEN 1 ELSE 0 END), 0)
         FROM PostVote v
         WHERE v.post.id = p.id),
        (SELECT COUNT(c.id) FROM Comment c WHERE c.post.id = p.id)
    )
        FROM Post p
        WHERE p.isPublished = true AND p.community.isPrivate = false
        ORDER BY p.createdAt DESC
    """)
    Page<PostWithVotesDTO> findPublicPosts(Pageable pageable);

    @Query("""
    SELECT new com.mb.reddit.dto.PostWithVotesDTO(
        p.id,
        p.title,
        p.content,
        p.mediaUrl,
        p.community.name,
        p.community.iconUrl,
        p.createdAt,
        (SELECT COALESCE(SUM(CASE WHEN v.isLike = true THEN 1 ELSE 0 END), 0)
         FROM PostVote v
         WHERE v.post.id = p.id),
        (SELECT COALESCE(SUM(CASE WHEN v.isLike = false THEN 1 ELSE 0 END), 0)
         FROM PostVote v
         WHERE v.post.id = p.id),
        (SELECT COUNT(c.id) FROM Comment c WHERE c.post.id = p.id)
    )
    FROM Post p
        JOIN p.community cm
        JOIN cm.members m
        WHERE p.isPublished = true 
          AND cm.isPrivate = true
          AND m.id = :userId
        ORDER BY p.createdAt DESC
    """)
    Page<PostWithVotesDTO> findPrivatePostsForUser(@Param("userId") Long userId, Pageable pageable);

    @Query("""
    SELECT new com.mb.reddit.dto.PostWithVotesDTO(
        p.id,
        p.title,
        p.content,
        p.mediaUrl,
        p.community.name,
        p.community.iconUrl,
        p.createdAt,
        (SELECT COUNT(v1) FROM PostVote v1 WHERE v1.post = p AND v1.isLike = true),
        (SELECT COUNT(v2) FROM PostVote v2 WHERE v2.post = p AND v2.isLike = false),
        (SELECT COUNT(c1) FROM Comment c1 WHERE c1.post = p)
    )
    FROM Post p
    WHERE p.isPublished = true AND p.community.isPrivate = false
    ORDER BY 
      (LOG10(GREATEST(ABS(
            (SELECT COUNT(v1) FROM PostVote v1 WHERE v1.post = p AND v1.isLike = true) - 
            (SELECT COUNT(v2) FROM PostVote v2 WHERE v2.post = p AND v2.isLike = false)
      ), 1)) +
       SIGN(
            (SELECT COUNT(v1) FROM PostVote v1 WHERE v1.post = p AND v1.isLike = true) - 
            (SELECT COUNT(v2) FROM PostVote v2 WHERE v2.post = p AND v2.isLike = false)
       ) * (EXTRACT(EPOCH FROM p.createdAt) - 1134028003) / 45000
      ) DESC
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
            WHERE p.id= :postId
            """)
    PostWithVotesDTO getPostWithVotesByPostId(@Param("postId") Long postId);
}

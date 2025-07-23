package com.mb.reddit.repository;

import com.mb.reddit.dto.UserKarmaDTO;
import com.mb.reddit.entity.Comment;
import com.mb.reddit.entity.Community;
import com.mb.reddit.entity.Post;
import com.mb.reddit.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT p FROM Post p WHERE p.author.id = :userId")
    Page<Post> findAllPostsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT p FROM User u JOIN u.savedPosts p WHERE u.id = :userId")
    Page<Post> findAllSavedPostsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.user.id = :userId")
    List<Comment> findAllCommentsByUserId(@Param("userId") Long userId);

    @Query("SELECT u.followers FROM User u WHERE u.id = :userId")
    List<User> findAllFollowersByUserId(@Param("userId") Long userId);

    @Query("SELECT u.following FROM User u WHERE u.id = :userId")
    List<User> findAllFollowingByUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM User u JOIN u.joinedCommunities c WHERE u.id = :userId")
    List<Community> findAllJoinedCommunitiesByUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM Community c WHERE c.creator.id = :userId")
    List<Community> findAllCreatedCommunitiesByUserId(@Param("userId") Long userId);

    @Query("""
    SELECT new com.mb.reddit.dto.UserKarmaDTO(
        u.id,
        u.username,
        COALESCE(SUM(CASE WHEN pv.isLike = true THEN 1 WHEN pv.isLike = false THEN -1 ELSE 0 END), 0),
        COALESCE(SUM(CASE WHEN cv.isLike = true THEN 1 WHEN cv.isLike = false THEN -1 ELSE 0 END), 0)
    )
    FROM User u
    LEFT JOIN Post p ON p.author.id = u.id
    LEFT JOIN PostVote pv ON pv.post.id = p.id
    LEFT JOIN Comment c ON c.user.id = u.id
    LEFT JOIN CommentVote cv ON cv.comment.id = c.id
    WHERE u.id = :userId
    GROUP BY u.id, u.username
    """)
    UserKarmaDTO getKarmaByUserId(@Param("userId") Long userId);

    User findUserByUsername(String name);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findUserByEmail(String email);
}

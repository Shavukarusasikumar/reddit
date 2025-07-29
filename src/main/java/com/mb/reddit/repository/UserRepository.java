package com.mb.reddit.repository;

import com.mb.reddit.dto.UserKarmaDTO;
import com.mb.reddit.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("""
    SELECT new com.mb.reddit.dto.UserKarmaDTO(
        u.id,
        u.username,
        (
            SELECT COALESCE(SUM(
                CASE 
                    WHEN pv.isLike = true THEN 1
                    WHEN pv.isLike = false THEN -1
                    ELSE 0
                END), 0)
            FROM Post p
            JOIN PostVote pv ON pv.post.id = p.id
            WHERE p.author.id = u.id
        ),
        (
            SELECT COALESCE(SUM(
                CASE 
                    WHEN cv.isLike = true THEN 1
                    WHEN cv.isLike = false THEN -1
                    ELSE 0
                END), 0)
            FROM Comment c
            JOIN CommentVote cv ON cv.comment.id = c.id
            WHERE c.user.id = u.id
        )
    )
    FROM User u
    WHERE u.id = :userId
    """)
    UserKarmaDTO getKarmaByUserId(@Param("userId") Long userId);

    @Query("SELECT u FROM User u WHERE u.username = :name")
    User findUserByUsername(@Param("name") String name);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.username = :username")
    boolean existsByUsername(@Param("username") String username);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email")
    boolean existsByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findUserByEmail(@Param("email") String email);
}

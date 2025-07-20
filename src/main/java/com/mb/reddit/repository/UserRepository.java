package com.mb.reddit.repository;

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

    User findUserByUsername(String name);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}

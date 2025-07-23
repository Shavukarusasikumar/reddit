package com.mb.reddit.repository;

import com.mb.reddit.entity.Notification;
import com.mb.reddit.entity.Post;
import com.mb.reddit.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n WHERE n.recipient = :user AND n.read = false")
    List<Notification> getUnreadNotifications(User user);

    @Query("SELECT n FROM Notification n WHERE n.recipient = :user ORDER BY n.timestamp DESC")
    List<Notification> getAllNotifications(User user);

    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :authorId AND n.post.id = :postId AND n.type = :type")
    Notification findTopByRecipientAndPostAndType(@Param("authorId") Long authorId,@Param("postId") Long postId,
                                                  String type);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipient.id = :userId")
    int countUnreadNotificationsByUserId(@Param("userId") Long userId);

    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :userId")
    Page<Notification> getAllNotificationsByUser(@Param("userId") Long userId, Pageable pageable);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.recipient = :userId AND n.read = false")
    void markAllAsReadForUser(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("DELETE from Notification n WHERE n.recipient.id = :userId")
    void deleteAllReadNotificationForUser(Long userId);
}


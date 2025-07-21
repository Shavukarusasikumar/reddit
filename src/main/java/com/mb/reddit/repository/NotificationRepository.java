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

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n WHERE n.recipient = :user AND n.read = false")
    List<Notification> getUnreadNotifications(User user);

    @Query("SELECT n FROM Notification n WHERE n.recipient = :user ORDER BY n.timestamp DESC")
    List<Notification> getAllNotifications(User user);

    @Query("SELECT n FROM Notification n WHERE n.recipient = :recipient AND n.post = :post AND n.type = :type")
    Notification findTopByRecipientAndPostAndType(User author, Post post, String type);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipient = :user AND n.read = false")
    int countUnreadNotifications(User user);

    @Query("SELECT n FROM Notification n WHERE n.recipient = :currentUser")
    Page<Notification> getAllNotificationsByUser(@Param("currentUser") User currentUser, Pageable pageable);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.recipient = :currentUser AND n.read = false")
    void markAllAsReadForUser(@Param("currentUser") User currentUser);
}


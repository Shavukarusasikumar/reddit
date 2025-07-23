package com.mb.reddit.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = {@Index(name = "idx_userid_read", columnList = "user_id")})
@Getter
@Setter
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;

    private boolean read = false;

    private String type; // "COMMENT" or "UPVOTE"

    private LocalDateTime timestamp;

    @Transient
    private Long postId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User recipient;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

}

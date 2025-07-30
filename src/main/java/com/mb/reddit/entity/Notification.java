package com.mb.reddit.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;

    private boolean read = false;

    private String type;

    private LocalDateTime timestamp;

    private Long senderId;

    private String senderName;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User recipient;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;
}
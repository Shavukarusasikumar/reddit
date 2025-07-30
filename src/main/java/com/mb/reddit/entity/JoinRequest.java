package com.mb.reddit.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "join_requests")
@Getter
@Setter
public class JoinRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Community community;

    @ManyToOne
    private User requester;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    private boolean approved;
}

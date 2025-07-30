package com.mb.reddit.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "communities")
@Getter
@Setter
public class Community {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String iconUrl;

    @Column(columnDefinition = "TEXT")
    private String bannerUrl;

    private Boolean isPrivate;

    private LocalDateTime createdAt;

    @ManyToOne
    private User creator;

    @OneToMany(mappedBy = "community", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    private List<Post> posts;

    @OneToMany(mappedBy = "community")
    private List<JoinRequest> joinRequests;

    @ManyToMany(mappedBy = "joinedCommunities", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<User> members;

    @ManyToMany
    @JoinTable(
            name = "community_topics",
            joinColumns = @JoinColumn(name = "community_id"),
            inverseJoinColumns = @JoinColumn(name = "topic_id")
    )
    private List<Topic> topics;
}
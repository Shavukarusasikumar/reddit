package com.mb.reddit.entity;

import jakarta.persistence.*;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "communities")
@Getter
@Setter
@NoArgsConstructor
public class Community {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @OneToMany(mappedBy = "community")
    private List<Post> posts;

    @OneToMany(mappedBy = "community")
    private List<JoinRequest> joinRequests;

    @OneToMany(mappedBy = "community")
    private List<Flair> flairs;

    @ManyToMany(mappedBy = "joinedCommunities", cascade = CascadeType.ALL)
    private List<User> members;

    @OneToMany(mappedBy = "community", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Topic> topics;
}
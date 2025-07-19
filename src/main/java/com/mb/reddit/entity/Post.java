package com.mb.reddit.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "posts")
@Data
@NoArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    @Column(name = "media_url")
    private String mediaUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_published")
    private Boolean isPublished;

    @ManyToOne
    private User author;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Community community;

    @OneToMany(mappedBy = "post")
    private List<Comment> comments;

    @OneToMany(mappedBy = "post")
    private List<PostVote> postVotes;

    @ManyToMany(mappedBy = "savedPosts", cascade = CascadeType.ALL)
    private List<User> savedByUser;
}
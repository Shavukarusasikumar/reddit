package com.mb.reddit.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Entity
@Table(name = "posts")
@Getter
@Setter
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title", columnDefinition = "TEXT")
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "media_url", columnDefinition = "TEXT")
    private String mediaUrl;

    @Column(name = "media_type")
    private String mediaType;

    @Column(name = "link_url")
    private String linkUrl;

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

    @OneToMany(mappedBy = "post", cascade = {CascadeType.REMOVE})
    private List<Comment> comments;

    @OneToMany(mappedBy = "post", cascade = {CascadeType.REMOVE})
    private List<PostVote> postVotes;

    @ManyToMany(mappedBy = "savedPosts")
    private List<User> savedByUser;

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE)
    private List<Notification> notifications;

    public String getShowTime() {
        if (this.createdAt == null) {
            return "Unknown time";
        }

        return this.createdAt.format(DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a"));
    }
}
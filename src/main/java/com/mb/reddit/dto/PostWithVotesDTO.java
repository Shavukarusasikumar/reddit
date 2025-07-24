package com.mb.reddit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@AllArgsConstructor
@Setter
public class PostWithVotesDTO {
    private Long id;
    private String title;
    private String content;
    private String mediaUrl;
    private String mediaType;
    private String linkUrl;
    private String communityName;
    private Long voteCount;
    private LocalDateTime createdAt;
    private String showTime;
    private Long commentCount;
    private Boolean isLiked;
    private String communityIconUrl;

    public PostWithVotesDTO(Long id, String title, String content, String mediaUrl, String mediaType, String linkUrl,
                            String communityName, String communityIconUrl, LocalDateTime createdAt,
                            Long upVotes, Long downVotes, Long commentCount) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.mediaUrl = mediaUrl;
        this.mediaType = mediaType;
        this.linkUrl = linkUrl;
        this.communityName = communityName;
        this.communityIconUrl = communityIconUrl;
        this.createdAt = createdAt;
        this.voteCount = (upVotes - downVotes);
        this.commentCount = commentCount != null ? commentCount : 0L;
        this.showTime = com.mb.reddit.utils.TimeAgoUtils.getTimeAgo(createdAt);
        this.isLiked = null;
    }
}

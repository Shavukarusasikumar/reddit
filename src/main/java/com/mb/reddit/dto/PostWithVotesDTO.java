package com.mb.reddit.dto;

import com.mb.reddit.entity.Post;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
public class PostWithVotesDTO {
    private Long id;
    private String title;
    private String content;
    private String mediaUrl;
    private String communityName;
    private int voteCount;
    private LocalDateTime createdAt;
    private String showTime;
    private Long commentCount;
    private Boolean isLiked;
    private String communityIconUrl;

    public PostWithVotesDTO(Long id, String title, String content, String mediaUrl,
                            String communityName, String communityIconUrl,
                            LocalDateTime createdAt, Long upVotes, Long downVotes, Long commentCount) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.mediaUrl = mediaUrl;
        this.communityName = communityName;
        this.communityIconUrl = communityIconUrl;
        this.voteCount = (int) (upVotes - downVotes);
        this.createdAt = createdAt;
        this.showTime = com.mb.reddit.utils.TimeAgoUtils.getTimeAgo(createdAt);
        this.commentCount = commentCount != null ? commentCount : 0L;
        this.isLiked = null;
    }
}

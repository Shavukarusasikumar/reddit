package com.mb.reddit.dto;

import com.mb.reddit.entity.Post;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import com.mb.reddit.utils.TimeAgoUtils;

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

    public PostWithVotesDTO(Long id, String title, String content, String mediaUrl,
                            String communityName, LocalDateTime createdAt,
                            Long upVotes, Long downVotes) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.mediaUrl = mediaUrl;
        this.communityName = communityName;
        this.voteCount = (int) (upVotes - downVotes);
        this.createdAt = createdAt;
        this.showTime = com.mb.reddit.utils.TimeAgoUtils.getTimeAgo(createdAt);
    }
}

package com.mb.reddit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class CommunityBasicDTO {
    private Long id;
    private String name;
    private String iconUrl;
}

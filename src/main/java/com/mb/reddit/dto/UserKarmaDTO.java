package com.mb.reddit.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserKarmaDTO {
    private Long userId;
    private String username;
    private Long postKarma;
    private Long commentKarma;
}
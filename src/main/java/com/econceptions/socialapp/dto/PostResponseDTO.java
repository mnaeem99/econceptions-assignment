package com.econceptions.socialapp.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostResponseDTO {
    private Long id;
    private Long userId;
    private String content;
    private LocalDateTime timestamp;
    private int commentCount;
    private int likeCount;
}
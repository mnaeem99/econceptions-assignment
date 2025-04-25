package com.econceptions.socialapp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostDTO {
    private Long id;
    private Long userId;
    
    @NotBlank(message = "Content is mandatory")
    private String content;
    
    private LocalDateTime timestamp;
    private int commentCount;
    private int likeCount;
}
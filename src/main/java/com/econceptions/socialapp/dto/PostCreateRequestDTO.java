package com.econceptions.socialapp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PostCreateRequestDTO {
    @NotBlank(message = "Content is mandatory")
    private String content;
}
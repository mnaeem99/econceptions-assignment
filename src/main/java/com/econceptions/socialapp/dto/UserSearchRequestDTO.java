package com.econceptions.socialapp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserSearchRequestDTO {
    @NotBlank(message = "Keyword is mandatory")
    private String keyword;
}
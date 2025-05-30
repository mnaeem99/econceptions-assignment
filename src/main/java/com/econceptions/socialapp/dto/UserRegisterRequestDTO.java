package com.econceptions.socialapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRegisterRequestDTO {
    @NotBlank(message = "Username is mandatory")
    private String username;

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is mandatory")
    private String password;

    private String bio;

    private String profilePicture;
}
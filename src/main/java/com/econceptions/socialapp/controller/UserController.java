package com.econceptions.socialapp.controller;

import com.econceptions.socialapp.dto.UserDTO;
import com.econceptions.socialapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@Tag(name = "User Management", description = "APIs for managing users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.register(userDTO));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and return JWT")
    public ResponseEntity<String> login(@RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.login(userDTO));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user profile by ID")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @PostMapping("/{id}/follow")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Follow a user")
    public ResponseEntity<Void> followUser(@PathVariable Long id) {
        userService.followUser(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/followers")
    @Operation(summary = "Get user's followers")
    public ResponseEntity<Page<UserDTO>> getFollowers(@PathVariable Long id,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.getFollowers(id, PageRequest.of(page, size)));
    }

    @GetMapping("/{id}/following")
    @Operation(summary = "Get users followed by a user")
    public ResponseEntity<Page<UserDTO>> getFollowing(@PathVariable Long id,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.getFollowing(id, PageRequest.of(page, size)));
    }

    @PostMapping("/search")
    @Operation(summary = "Search users by keyword")
    public ResponseEntity<Page<UserDTO>> searchUsers(@RequestBody String keyword,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.searchUsers(keyword, PageRequest.of(page, size)));
    }
}
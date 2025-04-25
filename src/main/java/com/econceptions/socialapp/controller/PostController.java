package com.econceptions.socialapp.controller;

import com.econceptions.socialapp.dto.PostCreateRequestDTO;
import com.econceptions.socialapp.dto.PostResponseDTO;
import com.econceptions.socialapp.dto.PostUpdateRequestDTO;
import com.econceptions.socialapp.dto.CommentRequestDTO;
import com.econceptions.socialapp.dto.PostSearchRequestDTO;
import com.econceptions.socialapp.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
@Tag(name = "Post Management", description = "APIs for managing posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a new post")
    public ResponseEntity<PostResponseDTO> createPost(@Valid @RequestBody PostCreateRequestDTO requestDTO) {
        return ResponseEntity.ok(postService.createPost(requestDTO));
    }

    @GetMapping
    @Operation(summary = "Get all posts with pagination and sorting")
    public ResponseEntity<Page<PostResponseDTO>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        return ResponseEntity.ok(postService.getAllPosts(PageRequest.of(page, size, sort)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get post by ID")
    public ResponseEntity<PostResponseDTO> getPost(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPost(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated() and @postService.isPostOwner(#id, authentication.name)")
    @Operation(summary = "Update a post")
    public ResponseEntity<PostResponseDTO> updatePost(@PathVariable Long id, @Valid @RequestBody PostUpdateRequestDTO requestDTO) {
        return ResponseEntity.ok(postService.updatePost(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated() and @postService.isPostOwner(#id, authentication.name)")
    @Operation(summary = "Delete a post")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/comments")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add comment to a post")
    public ResponseEntity<PostResponseDTO> addComment(@PathVariable Long id, @Valid @RequestBody CommentRequestDTO requestDTO) {
        return ResponseEntity.ok(postService.addComment(id, requestDTO));
    }

    @PostMapping("/{id}/like")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Like a post")
    public ResponseEntity<PostResponseDTO> likePost(@PathVariable Long id) {
        return ResponseEntity.ok(postService.likePost(id));
    }

    @PostMapping("/search")
    @Operation(summary = "Search posts by keyword")
    public ResponseEntity<Page<PostResponseDTO>> searchPosts(@Valid @RequestBody PostSearchRequestDTO requestDTO,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(postService.searchPosts(requestDTO, PageRequest.of(page, size)));
    }
}
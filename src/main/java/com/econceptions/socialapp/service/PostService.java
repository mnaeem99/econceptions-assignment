package com.econceptions.socialapp.service;

import com.econceptions.socialapp.dto.PostCreateRequestDTO;
import com.econceptions.socialapp.dto.PostResponseDTO;
import com.econceptions.socialapp.dto.PostUpdateRequestDTO;
import com.econceptions.socialapp.dto.CommentRequestDTO;
import com.econceptions.socialapp.dto.PostSearchRequestDTO;
import com.econceptions.socialapp.entity.Comment;
import com.econceptions.socialapp.entity.Post;
import com.econceptions.socialapp.entity.User;
import com.econceptions.socialapp.repository.CommentRepository;
import com.econceptions.socialapp.repository.PostRepository;
import com.econceptions.socialapp.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository,
                       CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
    }

    public PostResponseDTO createPost(PostCreateRequestDTO requestDTO) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = new Post();
        post.setUser(user);
        post.setContent(requestDTO.getContent());
        post = postRepository.save(post);
        return mapToResponseDTO(post);
    }

    public Page<PostResponseDTO> getAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable).map(this::mapToResponseDTO);
    }

    public PostResponseDTO getPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return mapToResponseDTO(post);
    }

    public PostResponseDTO updatePost(Long id, PostUpdateRequestDTO requestDTO) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        post.setContent(requestDTO.getContent());
        post = postRepository.save(post);
        return mapToResponseDTO(post);
    }

    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        postRepository.delete(post);
    }

    public PostResponseDTO addComment(Long id, CommentRequestDTO requestDTO) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setContent(requestDTO.getContent());
        commentRepository.save(comment);

        return mapToResponseDTO(post);
    }

    public PostResponseDTO likePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!post.getLikes().contains(user)) {
            post.getLikes().add(user);
            post = postRepository.save(post);
        }

        return mapToResponseDTO(post);
    }

    public Page<PostResponseDTO> searchPosts(PostSearchRequestDTO requestDTO, Pageable pageable) {
        return postRepository.findByContentContaining(requestDTO.getKeyword(), pageable)
                .map(this::mapToResponseDTO);
    }

    public boolean isPostOwner(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return post.getUser().getUsername().equals(username);
    }

    private PostResponseDTO mapToResponseDTO(Post post) {
        PostResponseDTO dto = new PostResponseDTO();
        dto.setId(post.getId());
        dto.setUserId(post.getUser().getId());
        dto.setContent(post.getContent());
        dto.setTimestamp(post.getTimestamp());
        dto.setCommentCount(post.getComments().size());
        dto.setLikeCount(post.getLikes().size());
        return dto;
    }
}
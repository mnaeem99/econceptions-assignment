package com.econceptions.socialapp.service;

import com.econceptions.socialapp.dto.PostDTO;
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

    public PostDTO createPost(PostDTO postDTO) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Post post = new Post();
        post.setUser(user);
        post.setContent(postDTO.getContent());
        post = postRepository.save(post);
        return mapToDTO(post);
    }

    public Page<PostDTO> getAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable).map(this::mapToDTO);
    }

    public PostDTO getPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return mapToDTO(post);
    }

    public PostDTO updatePost(Long id, PostDTO postDTO) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        post.setContent(postDTO.getContent());
        post = postRepository.save(post);
        return mapToDTO(post);
    }

    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        postRepository.delete(post);
    }

    public PostDTO addComment(Long id, String content) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setContent(content);
        commentRepository.save(comment);

        return mapToDTO(post);
    }

    public PostDTO likePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!post.getLikes().contains(user)) {
            post.getLikes().add(user);
            post = postRepository.save(post);
        }

        return mapToDTO(post);
    }

    public Page<PostDTO> searchPosts(String keyword, Pageable pageable) {
        return postRepository.findByContentContaining(keyword, pageable)
                .map(this::mapToDTO);
    }

    public boolean isPostOwner(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return post.getUser().getUsername().equals(username);
    }

    private PostDTO mapToDTO(Post post) {
        PostDTO dto = new PostDTO();
        dto.setId(post.getId());
        dto.setUserId(post.getUser().getId());
        dto.setContent(post.getContent());
        dto.setTimestamp(post.getTimestamp());
        dto.setCommentCount(post.getComments().size());
        dto.setLikeCount(post.getLikes().size());
        return dto;
    }
}
package com.econceptions.socialapp.service;

import com.econceptions.socialapp.dto.*;
import com.econceptions.socialapp.entity.*;
import com.econceptions.socialapp.repository.*;
import com.econceptions.socialapp.service.PostService;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PostServiceTests {

    @Mock private PostRepository postRepository;
    @Mock private UserRepository userRepository;
    @Mock private CommentRepository commentRepository;

    @InjectMocks private PostService postService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        var securityContext = mock(org.springframework.security.core.context.SecurityContext.class);
        var authentication = mock(org.springframework.security.core.Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testUser");
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testCreatePost_UserNotFound() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> postService.createPost(new PostCreateRequestDTO()))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("User not found");
    }

    @Test
    void testGetPost_NotFound() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> postService.getPost(99L))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("Post not found");
    }

    @Test
    void testUpdatePost_NotFound() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> postService.updatePost(99L, new PostUpdateRequestDTO()))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("Post not found");
    }

    @Test
    void testDeletePost_NotFound() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> postService.deletePost(99L))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("Post not found");
    }

    @Test
    void testAddComment_PostNotFound() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> postService.addComment(99L, new CommentRequestDTO()))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("Post not found");
    }


    @Test
    void testLikePost() {
        Post post = new Post();
        post.setId(1L);
        post.setUser(new User());
        List<User> likes = new ArrayList<User>();
        User user = new User();
        user.setUsername("testUser");
        likes.add(user);
        post.setLikes(likes);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(postRepository.save(post)).thenReturn(post);

        PostResponseDTO dto = postService.likePost(1L);
        assertThat(dto.getLikeCount()).isEqualTo(1);
    }

    @Test
    void testLikePost_UserNotFound() {
        Post post = new Post(); post.setId(1L);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.likePost(1L))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("User not found");
    }

    @Test
    void testSearchPosts_EmptyResult() {
        Page<Post> emptyPage = Page.empty();
        when(postRepository.findByContentContaining("none", PageRequest.of(0, 10)))
                .thenReturn(emptyPage);

        PostSearchRequestDTO dto = new PostSearchRequestDTO(); dto.setKeyword("none");
        Page<PostResponseDTO> result = postService.searchPosts(dto, PageRequest.of(0, 10));
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void testIsPostOwner_InvalidOwner() {
        User user = new User(); user.setUsername("someone");
        Post post = new Post(); post.setUser(user);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        assertThat(postService.isPostOwner(1L, "testUser")).isFalse();
    }
}

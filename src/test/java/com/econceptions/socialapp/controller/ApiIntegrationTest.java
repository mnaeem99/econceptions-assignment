package com.econceptions.socialapp.controller;

import com.econceptions.socialapp.dto.*;
import com.econceptions.socialapp.entity.Comment;
import com.econceptions.socialapp.entity.Post;
import com.econceptions.socialapp.entity.User;
import com.econceptions.socialapp.repository.CommentRepository;
import com.econceptions.socialapp.repository.PostRepository;
import com.econceptions.socialapp.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
@ActiveProfiles("test")
class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    protected String jwtToken;

    @BeforeAll
    void setUp() throws Exception {

        // Creating a test user
        User user = new User();
        user.setUsername("naeem");
        user.setPassword("$2a$12$Mct9TaQEj2RM1XjYP5rxEOVGKES8mCYe7txshhc0VR059Wsk0wjxm");
        user.setEmail("naeem@example.com");
        User newUser = userRepository.save(user);

        User anotherUser = new User();
        anotherUser.setUsername("testing");
        anotherUser.setPassword("$2a$12$Mct9TaQEj2RM1XjYP5rxEOVGKES8mCYe7txshhc0VR059Wsk0wjxm");
        anotherUser.setEmail("testing@example.com");
        userRepository.save(anotherUser);

        // Create some posts
        Post post = new Post();
        post.setUser(newUser);
        post.setContent("Test Content for Post 1");
        Post newPost = postRepository.save(post);

        // Create comments for the post
        Comment comment = new Comment();
        comment.setPost(newPost);
        comment.setUser(newUser);
        comment.setContent("Test Comment on Post 1");
        commentRepository.save(comment);

        // Create login request
        UserLoginRequestDTO loginRequest = new UserLoginRequestDTO();
        loginRequest.setUsername("testing");  // <- login with `testing` user
        loginRequest.setPassword("pass123");

        String response = mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        jwtToken = "Bearer " + response;

    }

    protected RequestPostProcessor auth() {
        return request -> {
            request.addHeader("Authorization", jwtToken);
            return request;
        };
    }

    @Test
    void testCreatePost() throws Exception {
        PostCreateRequestDTO request = new PostCreateRequestDTO();
        request.setContent("Test Content");

        mockMvc.perform(post("/posts").with(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllPosts() throws Exception {
        mockMvc.perform(get("/posts").with(auth())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetPostById() throws Exception {
        mockMvc.perform(get("/posts/{id}", 1).with(auth()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testUpdatePost() throws Exception {
        PostUpdateRequestDTO request = new PostUpdateRequestDTO();
        request.setContent("Updated Content");

        mockMvc.perform(put("/posts/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testDeletePost() throws Exception {
        mockMvc.perform(delete("/posts/{id}", 1))
                .andExpect(status().isOk());
    }

    @Test
    void testAddComment() throws Exception {
        CommentRequestDTO request = new CommentRequestDTO();
        request.setContent("Nice post!");

        mockMvc.perform(post("/posts/{id}/comments", 1).with(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testSearchPosts() throws Exception {
        PostSearchRequestDTO request = new PostSearchRequestDTO();
        request.setKeyword("t");

        mockMvc.perform(post("/posts/search").with(auth())
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
    @Test
    void register_shouldReturnUser() throws Exception {
        UserRegisterRequestDTO request = new UserRegisterRequestDTO();
        request.setUsername("mnaeem");
        request.setPassword("pass123");
        request.setEmail("mnaeem@example.com");

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("mnaeem"));
    }

    @Test
    void login_shouldReturnJwt() throws Exception {
        UserLoginRequestDTO request = new UserLoginRequestDTO();
        request.setUsername("naeem");
        request.setPassword("pass123");

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void getUser_shouldReturnUserProfile() throws Exception {
        Long userId = 2L;
        mockMvc.perform(get("/users/" + userId).with(auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId));
    }

    @Test
    void followUser_shouldSucceed() throws Exception {
        Long userId = 1L;
        mockMvc.perform(post("/users/" + userId + "/follow").with(auth()))
                .andExpect(status().isOk());
    }

    @Test
    void searchUsers_shouldReturnResults() throws Exception {
        UserSearchRequestDTO request = new UserSearchRequestDTO();
        request.setKeyword("n");

        mockMvc.perform(post("/users/search?page=0&size=10").with(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

}

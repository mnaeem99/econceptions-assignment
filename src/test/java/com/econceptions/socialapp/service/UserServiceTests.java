package com.econceptions.socialapp.service;

import com.econceptions.socialapp.dto.*;
import com.econceptions.socialapp.entity.*;
import com.econceptions.socialapp.repository.*;
import com.econceptions.socialapp.util.JwtUtil;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTests {

    @Mock private UserRepository userRepository;
    @Mock private FollowRepository followRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks private UserService userService;

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
    void testRegister_DuplicateUsername() {
        when(userRepository.save(any())).thenThrow(new RuntimeException("Duplicate username"));
        UserRegisterRequestDTO dto = new UserRegisterRequestDTO();
        dto.setUsername("testUser");
        dto.setPassword("123");
        assertThatThrownBy(() -> userService.register(dto)).hasMessageContaining("Duplicate");
    }

    @Test
    void testLogin_UserNotFound() {
        when(userRepository.findByUsername("nouser")).thenReturn(Optional.empty());
        UserLoginRequestDTO dto = new UserLoginRequestDTO();
        dto.setUsername("nouser"); dto.setPassword("123");
        assertThatThrownBy(() -> userService.login(dto)).hasMessageContaining("User not found");
    }

    @Test
    void testLogin_WrongPassword() {
        User user = new User(); user.setPassword("encoded");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        UserLoginRequestDTO dto = new UserLoginRequestDTO();
        dto.setUsername("user"); dto.setPassword("wrong");
        assertThatThrownBy(() -> userService.login(dto)).hasMessageContaining("Invalid credentials");
    }

    @Test
    void testGetUser_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getUser(99L)).hasMessageContaining("User not found");
    }

    @Test
    void testFollowUser_AlreadyFollowing() {
        User follower = new User();
        follower.setUsername("testUser");
        follower.setId(1L);
        User following = new User();
        following.setId(2L);
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(follower));
        when(userRepository.findById(2L)).thenReturn(Optional.of(following));
        when(followRepository.existsByFollowerAndFollowing(follower, following)).thenReturn(true);

        assertThatThrownBy(() -> userService.followUser(2L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Already following this user");

        verify(followRepository, never()).save(any());
    }

    @Test
    void testLoadUserByUsername_NotFound() {
        when(userRepository.findByUsername("nouser")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.loadUserByUsername("nouser"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void testSearchUsers_Empty() {
        Page<User> emptyPage = Page.empty();
        when(userRepository.findByUsernameContainingOrEmailContainingOrBioContaining(
                "missing", "missing", "missing", PageRequest.of(0, 10))).thenReturn(emptyPage);

        UserSearchRequestDTO dto = new UserSearchRequestDTO();
        dto.setKeyword("missing");
        Page<UserResponseDTO> result = userService.searchUsers(dto, PageRequest.of(0, 10));
        assertThat(result.getContent()).isEmpty();
    }
}

package com.econceptions.socialapp.service;

import com.econceptions.socialapp.dto.UserRegisterRequestDTO;
import com.econceptions.socialapp.dto.UserResponseDTO;
import com.econceptions.socialapp.dto.UserLoginRequestDTO;
import com.econceptions.socialapp.dto.UserSearchRequestDTO;
import com.econceptions.socialapp.entity.Follow;
import com.econceptions.socialapp.entity.User;
import com.econceptions.socialapp.repository.FollowRepository;
import com.econceptions.socialapp.repository.UserRepository;
import com.econceptions.socialapp.util.JwtUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, FollowRepository followRepository,
                       PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public UserResponseDTO register(UserRegisterRequestDTO requestDTO) {
        User user = new User();
        user.setUsername(requestDTO.getUsername());
        user.setEmail(requestDTO.getEmail());
        user.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        user.setBio(requestDTO.getBio());
        user.setProfilePicture(requestDTO.getProfilePicture());
        user = userRepository.save(user);
        return mapToResponseDTO(user);
    }

    public String login(UserLoginRequestDTO requestDTO) {
        User user = userRepository.findByUsername(requestDTO.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (passwordEncoder.matches(requestDTO.getPassword(), user.getPassword())) {
            return jwtUtil.generateToken(user.getUsername());
        }
        throw new BadCredentialsException("Invalid credentials");
    }

    public UserResponseDTO getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToResponseDTO(user);
    }

    public void followUser(Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User follower = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Follower not found"));
        User following = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User to follow not found"));

        if (follower.getId().equals(following.getId())) {
            throw new RuntimeException("Cannot follow yourself");
        }

        if (!followRepository.existsByFollowerAndFollowing(follower, following)) {
            Follow follow = new Follow();
            follow.setFollower(follower);
            follow.setFollowing(following);
            followRepository.save(follow);
        } else {
            throw new RuntimeException("Already following this user");
        }
    }

    public Page<UserResponseDTO> getFollowers(Long id, Pageable pageable) {
        return followRepository.findByFollowingId(id, pageable)
                .map(follow -> mapToResponseDTO(follow.getFollower()));
    }

    public Page<UserResponseDTO> getFollowing(Long id, Pageable pageable) {
        return followRepository.findByFollowerId(id, pageable)
                .map(follow -> mapToResponseDTO(follow.getFollowing()));
    }

    public Page<UserResponseDTO> searchUsers(UserSearchRequestDTO requestDTO, Pageable pageable) {
        return userRepository.findByUsernameContainingOrEmailContainingOrBioContaining(
                        requestDTO.getKeyword(), requestDTO.getKeyword(), requestDTO.getKeyword(), pageable)
                .map(this::mapToResponseDTO);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities("USER")
                .build();
    }

    private UserResponseDTO mapToResponseDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setBio(user.getBio());
        dto.setProfilePicture(user.getProfilePicture());
        return dto;
    }
}
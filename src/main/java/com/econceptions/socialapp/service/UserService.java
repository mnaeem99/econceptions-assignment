package com.econceptions.socialapp.service;

import com.econceptions.socialapp.dto.UserDTO;
import com.econceptions.socialapp.entity.Follow;
import com.econceptions.socialapp.entity.User;
import com.econceptions.socialapp.repository.FollowRepository;
import com.econceptions.socialapp.repository.UserRepository;
import com.econceptions.socialapp.util.JwtUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public UserDTO register(UserDTO userDTO) {
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setBio(userDTO.getBio());
        user.setProfilePicture(userDTO.getProfilePicture());
        user = userRepository.save(user);
        return mapToDTO(user);
    }

    public String login(UserDTO userDTO) {
        User user = userRepository.findByUsername(userDTO.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (passwordEncoder.matches(userDTO.getPassword(), user.getPassword())) {
            return jwtUtil.generateToken(user.getUsername());
        }
        throw new RuntimeException("Invalid credentials");
    }

    public UserDTO getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToDTO(user);
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

    public Page<UserDTO> getFollowers(Long id, Pageable pageable) {
        return followRepository.findByFollowingId(id, pageable)
                .map(follow -> mapToDTO(follow.getFollower()));
    }

    public Page<UserDTO> getFollowing(Long id, Pageable pageable) {
        return followRepository.findByFollowerId(id, pageable)
                .map(follow -> mapToDTO(follow.getFollowing()));
    }

    public Page<UserDTO> searchUsers(String keyword, Pageable pageable) {
        return userRepository.findByUsernameContainingOrEmailContainingOrBioContaining(
                        keyword, keyword, keyword, pageable)
                .map(this::mapToDTO);
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

    private UserDTO mapToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setBio(user.getBio());
        dto.setProfilePicture(user.getProfilePicture());
        return dto;
    }
}
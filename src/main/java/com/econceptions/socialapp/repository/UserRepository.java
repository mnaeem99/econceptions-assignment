package com.econceptions.socialapp.repository;

import com.econceptions.socialapp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Page<User> findByUsernameContainingOrEmailContainingOrBioContaining(
            String username, String email, String bio, Pageable pageable);
}
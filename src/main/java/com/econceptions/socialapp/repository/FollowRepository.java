package com.econceptions.socialapp.repository;

import com.econceptions.socialapp.entity.Follow;
import com.econceptions.socialapp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFollowerAndFollowing(User follower, User following);
    Page<Follow> findByFollowerId(Long followerId, Pageable pageable);
    Page<Follow> findByFollowingId(Long followingId, Pageable pageable);
}
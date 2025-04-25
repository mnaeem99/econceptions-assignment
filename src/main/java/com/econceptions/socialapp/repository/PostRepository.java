package com.econceptions.socialapp.repository;

import com.econceptions.socialapp.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByContentContaining(String content, Pageable pageable);
}
package com.econceptions.socialapp.repository;

import com.econceptions.socialapp.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
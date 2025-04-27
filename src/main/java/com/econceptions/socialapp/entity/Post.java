package com.econceptions.socialapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String content;

    private LocalDateTime timestamp = LocalDateTime.now();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();

    //this only work in postgresql and not work in mysql because mysql not allow table with primary key
//    @ManyToMany
//    @JoinTable(
//        name = "post_likes",
//        joinColumns = @JoinColumn(name = "post_id"),
//        inverseJoinColumns = @JoinColumn(name = "user_id"),
//    )
//    private List<User> likes = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostLike> likes = new ArrayList<>();

}
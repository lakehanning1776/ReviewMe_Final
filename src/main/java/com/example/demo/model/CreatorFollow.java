package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "creator_follows", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "creator_id"}))
public class CreatorFollow {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) private User user;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "creator_id", nullable = false) private CreatorProfile creator;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt = LocalDateTime.now();
    public CreatorFollow() {}
    public Long getId(){return id;} public User getUser(){return user;} public void setUser(User v){user=v;}
    public CreatorProfile getCreator(){return creator;} public void setCreator(CreatorProfile v){creator=v;}
    public LocalDateTime getCreatedAt(){return createdAt;}
}

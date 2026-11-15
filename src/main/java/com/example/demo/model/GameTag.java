package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "game_tags", uniqueConstraints = @UniqueConstraint(columnNames = "normalized_name"))
public class GameTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String name;

    @Column(name = "normalized_name", nullable = false, length = 32, unique = true)
    private String normalizedName;

    protected GameTag() { }

    public GameTag(String name) {
        this.name = name;
        this.normalizedName = name.trim().toLowerCase(java.util.Locale.ROOT);
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getNormalizedName() { return normalizedName; }
}

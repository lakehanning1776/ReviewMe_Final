package com.example.demo.repository;

import com.example.demo.model.GameTag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GameTagRepository extends JpaRepository<GameTag, Long> {
    Optional<GameTag> findByNormalizedName(String normalizedName);
}

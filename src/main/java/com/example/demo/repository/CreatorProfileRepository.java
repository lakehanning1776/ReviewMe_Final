package com.example.demo.repository;

import com.example.demo.model.CreatorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CreatorProfileRepository extends JpaRepository<CreatorProfile, Long> {
    Optional<CreatorProfile> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}

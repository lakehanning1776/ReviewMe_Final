package com.example.demo.repository;
import com.example.demo.model.CreatorFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface CreatorFollowRepository extends JpaRepository<CreatorFollow, Long> {
 boolean existsByUserIdAndCreatorId(Long userId, Long creatorId);
 long countByCreatorId(Long creatorId);
 List<CreatorFollow> findByUserIdOrderByCreatedAtDesc(Long userId);
 java.util.Optional<CreatorFollow> findByUserIdAndCreatorId(Long userId, Long creatorId);
}

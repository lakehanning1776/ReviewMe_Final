package com.example.demo.repository;

import com.example.demo.model.CreatorReview;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CreatorReviewRepository extends JpaRepository<CreatorReview, Long> {
    List<CreatorReview> findByGameIdOrderByCreatedAtDesc(Long gameId);
    List<CreatorReview> findByCreatorIdOrderByCreatedAtDesc(Long creatorId);
}

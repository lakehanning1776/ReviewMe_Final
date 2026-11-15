package com.example.demo.repository;

import com.example.demo.model.CreatorCode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CreatorCodeRepository extends JpaRepository<CreatorCode, Long> {
    List<CreatorCode> findByCreatorIdOrderByCreatedAtDesc(Long creatorId);
    List<CreatorCode> findByGameIdOrderByCreatedAtDesc(Long gameId);
}

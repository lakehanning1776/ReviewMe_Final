package com.example.demo.repository;
import com.example.demo.model.ContentReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ContentReportRepository extends JpaRepository<ContentReport,Long> {
 List<ContentReport> findByStatusOrderByCreatedAtDesc(String status);
}

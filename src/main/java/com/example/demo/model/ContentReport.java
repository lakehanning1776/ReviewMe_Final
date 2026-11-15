package com.example.demo.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="content_reports")
public class ContentReport {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false, fetch=FetchType.LAZY) @JoinColumn(name="reporter_id", nullable=false) private User reporter;
 @Column(name="target_type", nullable=false, length=20) private String targetType;
 @Column(name="target_id", nullable=false) private Long targetId;
 @Column(nullable=false, length=80) private String reason;
 @Column(length=2000) private String details;
 @Column(nullable=false, length=20) private String status="OPEN";
 @Column(name="created_at", nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
 @Column(name="resolved_at") private LocalDateTime resolvedAt;
 public ContentReport(){}
 public Long getId(){return id;} public User getReporter(){return reporter;} public void setReporter(User v){reporter=v;}
 public String getTargetType(){return targetType;} public void setTargetType(String v){targetType=v;}
 public Long getTargetId(){return targetId;} public void setTargetId(Long v){targetId=v;}
 public String getReason(){return reason;} public void setReason(String v){reason=v;}
 public String getDetails(){return details;} public void setDetails(String v){details=v;}
 public String getStatus(){return status;} public void setStatus(String v){status=v;}
 public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getResolvedAt(){return resolvedAt;} public void setResolvedAt(LocalDateTime v){resolvedAt=v;}
}

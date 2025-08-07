package com.codegym.module4casestudy.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "registration_periods")
public class RegistrationPeriod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "period_name", nullable = false, length = 100)
    private String periodName; // VD: "Đăng ký môn học Kỳ 1 năm 2024"

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime; // Thời gian bắt đầu đăng ký

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime; // Thời gian kết thúc đăng ký

    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // Mô tả chi tiết

    @Column(name = "max_subjects_per_student")
    private Integer maxSubjectsPerStudent; // Số môn tối đa sinh viên có thể đăng ký

    @Column(name = "max_credits_per_student")
    private Integer maxCreditsPerStudent; // Số tín chỉ tối đa sinh viên có thể đăng ký

    // Trạng thái của kỳ đăng ký
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private RegistrationStatus status = RegistrationStatus.SCHEDULED;

    // Ai tạo kỳ đăng ký
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "active", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean active = true;

    // Enum cho trạng thái đăng ký
    public enum RegistrationStatus {
        SCHEDULED,  // Đã lên kế hoạch
        OPEN,       // Đang mở đăng ký
        CLOSED,     // Đã đóng đăng ký
        CANCELLED   // Đã hủy
    }

    // Constructors
    public RegistrationPeriod() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public RegistrationPeriod(String periodName, LocalDateTime startTime, LocalDateTime endTime) {
        this();
        this.periodName = periodName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = RegistrationStatus.SCHEDULED;
        this.active = true;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPeriodName() {
        return periodName;
    }

    public void setPeriodName(String periodName) {
        this.periodName = periodName;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getMaxSubjectsPerStudent() {
        return maxSubjectsPerStudent;
    }

    public void setMaxSubjectsPerStudent(Integer maxSubjectsPerStudent) {
        this.maxSubjectsPerStudent = maxSubjectsPerStudent;
    }

    public Integer getMaxCreditsPerStudent() {
        return maxCreditsPerStudent;
    }

    public void setMaxCreditsPerStudent(Integer maxCreditsPerStudent) {
        this.maxCreditsPerStudent = maxCreditsPerStudent;
    }

    public RegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(RegistrationStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    // Helper methods
    public boolean isCurrentlyOpen() {
        LocalDateTime now = LocalDateTime.now();
        return status == RegistrationStatus.OPEN && 
               now.isAfter(startTime) && 
               now.isBefore(endTime);
    }

    public boolean isUpcoming() {
        LocalDateTime now = LocalDateTime.now();
        return status == RegistrationStatus.SCHEDULED && 
               now.isBefore(startTime);
    }

    public boolean isClosed() {
        LocalDateTime now = LocalDateTime.now();
        return status == RegistrationStatus.CLOSED || 
               now.isAfter(endTime);
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

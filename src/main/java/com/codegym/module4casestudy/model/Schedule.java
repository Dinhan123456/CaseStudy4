package com.codegym.module4casestudy.model;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "schedules", 
       uniqueConstraints = {
           @UniqueConstraint(name = "unique_teacher_schedule", 
                           columnNames = {"teacher_id", "day_of_week", "time_slot_id", "start_date", "end_date"}),
           @UniqueConstraint(name = "unique_room_schedule", 
                           columnNames = {"room_id", "day_of_week", "time_slot_id", "start_date", "end_date"}),
           @UniqueConstraint(name = "unique_class_schedule", 
                           columnNames = {"class_id", "day_of_week", "time_slot_id", "start_date", "end_date"})
       })
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Lớp học
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private Class classEntity;

    // Môn học
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    // Giảng viên
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    // Phòng học
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    // Khung giờ
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_slot_id", nullable = false)
    private TimeSlot timeSlot;

    // Ngày học (0=CN, 1=T2, 2=T3, 3=T4, 4=T5, 5=T6, 6=T7)
    @Column(name = "day_of_week", nullable = false, columnDefinition = "TINYINT")
    private Integer dayOfWeek;

    // Ngày bắt đầu hiệu lực
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    // Ngày kết thúc hiệu lực
    @Column(name = "end_date")
    private LocalDate endDate;

    // Trạng thái lịch
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private ScheduleStatus status = ScheduleStatus.PENDING;

    // Ghi chú
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Ai tạo lịch (FK đến users)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    // Thời gian tạo
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Thời gian cập nhật
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "active", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean active = true;

    // Enum cho trạng thái lịch
    public enum ScheduleStatus {
        PENDING,    // Chờ xác nhận
        CONFIRMED,  // Đã xác nhận
        CANCELLED,  // Đã hủy
        COMPLETED   // Đã hoàn thành
    }

    // Constructors
    public Schedule() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Schedule(Class classEntity, Subject subject, User teacher, Room room, 
                   TimeSlot timeSlot, Integer dayOfWeek, LocalDate startDate) {
        this();
        this.classEntity = classEntity;
        this.subject = subject;
        this.teacher = teacher;
        this.room = room;
        this.timeSlot = timeSlot;
        this.dayOfWeek = dayOfWeek;
        this.startDate = startDate;
        this.status = ScheduleStatus.PENDING;
        this.active = true;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Class getClassEntity() {
        return classEntity;
    }

    public void setClassEntity(Class classEntity) {
        this.classEntity = classEntity;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public User getTeacher() {
        return teacher;
    }

    public void setTeacher(User teacher) {
        this.teacher = teacher;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }

    public Integer getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(Integer dayOfWeek) {
        if (dayOfWeek == null || dayOfWeek < 0 || dayOfWeek > 6) {
            throw new IllegalArgumentException("Ngày trong tuần phải từ 0 (Chủ nhật) đến 6 (Thứ 7). Giá trị nhận được: " + dayOfWeek);
        }
        this.dayOfWeek = dayOfWeek;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public ScheduleStatus getStatus() {
        return status;
    }

    public void setStatus(ScheduleStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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
    public String getDayOfWeekName() {
        switch (dayOfWeek) {
            case 0: return "Chủ nhật";
            case 1: return "Thứ 2";
            case 2: return "Thứ 3";
            case 3: return "Thứ 4";
            case 4: return "Thứ 5";
            case 5: return "Thứ 6";
            case 6: return "Thứ 7";
            default: return "Không xác định";
        }
    }

    public String getDayOfWeekShort() {
        switch (dayOfWeek) {
            case 0: return "CN";
            case 1: return "T2";
            case 2: return "T3";
            case 3: return "T4";
            case 4: return "T5";
            case 5: return "T6";
            case 6: return "T7";
            default: return "??";
        }
    }

    public String getDisplayTime() {
        return timeSlot != null ? timeSlot.getTimeRange() : "";
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

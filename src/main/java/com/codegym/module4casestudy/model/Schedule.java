package com.codegym.module4casestudy.model;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "schedules",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "unique_teacher_schedule",
                        columnNames = {"teacher_id", "day_of_week", "time_slot_id", "start_date", "end_date"}
                ),
                @UniqueConstraint(
                        name = "unique_room_schedule",
                        columnNames = {"room_id", "day_of_week", "time_slot_id", "start_date", "end_date"}
                ),
                @UniqueConstraint(
                        name = "unique_class_schedule",
                        columnNames = {"class_id", "day_of_week", "time_slot_id", "start_date", "end_date"}
                )
        }
)
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Lớp học
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private Class classEntity; // tránh đụng java.lang.Class

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

    // ===== DÙNG ENUM TRÙNG VỚI DDL =====
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 3)
    private WeekDay dayOfWeek;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private ScheduleStatus status = ScheduleStatus.PENDING;
    // ===================================

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Ai tạo lịch (FK users)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at", updatable = false, insertable = false,
            columnDefinition = "timestamp default current_timestamp")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false,
            columnDefinition = "timestamp default current_timestamp on update current_timestamp")
    private LocalDateTime updatedAt;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    // ===== Constructors =====
    public Schedule() {}

    public Schedule(Class classEntity, Subject subject, User teacher, Room room,
                    TimeSlot timeSlot, WeekDay dayOfWeek,
                    LocalDate startDate, LocalDate endDate, User createdBy) {
        this.classEntity = classEntity;
        this.subject = subject;
        this.teacher = teacher;
        this.room = room;
        this.timeSlot = timeSlot;
        this.dayOfWeek = dayOfWeek;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdBy = createdBy;
        this.status = ScheduleStatus.PENDING;
        this.active = true;
    }

    // ===== Getters/Setters =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Class getClassEntity() { return classEntity; }
    public void setClassEntity(Class classEntity) { this.classEntity = classEntity; }

    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }

    public User getTeacher() { return teacher; }
    public void setTeacher(User teacher) { this.teacher = teacher; }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }

    public TimeSlot getTimeSlot() { return timeSlot; }
    public void setTimeSlot(TimeSlot timeSlot) { this.timeSlot = timeSlot; }

    public WeekDay getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(WeekDay dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public ScheduleStatus getStatus() { return status; }
    public void setStatus(ScheduleStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    // ===== Helpers cho UI =====
    @Transient
    public String getDayOfWeekVn() {
        return dayOfWeek == null ? "" : dayOfWeek.vnLabel();
    }

    @Transient
    public String getDayOfWeekShort() {
        return dayOfWeek == null ? "" : dayOfWeek.shortLabel();
    }

    @Transient
    public String getDisplayTime() {
        return timeSlot != null ? timeSlot.getTimeRange() : "";
    }
}

package com.codegym.module4casestudy.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_registrations")
public class StudentRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Sinh viên đăng ký
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    // Lịch học đăng ký
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = true)
    private Schedule schedule;

    // Môn học (đăng ký theo môn, không nhất thiết có lịch ngay)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    // Lớp học gắn với đăng ký môn (nếu có)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private Class classEntity;

    // Kỳ đăng ký
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_period_id", nullable = false)
    private RegistrationPeriod registrationPeriod;

    // Trạng thái đăng ký
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private RegistrationStatus status = RegistrationStatus.REGISTERED;

    // Thời gian đăng ký
    @Column(name = "registered_at")
    private LocalDateTime registeredAt;

    // Ghi chú
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "active", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean active = true;

    // Enum cho trạng thái đăng ký
    public enum RegistrationStatus {
        REGISTERED,   // Đã đăng ký
        CONFIRMED,    // Đã xác nhận
        WAITLISTED,   // Trong danh sách chờ
        CANCELLED,    // Đã hủy
        COMPLETED     // Hoàn thành
    }

    // Constructors
    public StudentRegistration() {
        this.registeredAt = LocalDateTime.now();
    }

    public StudentRegistration(User student, Schedule schedule, RegistrationPeriod registrationPeriod) {
        this();
        this.student = student;
        this.schedule = schedule;
        this.registrationPeriod = registrationPeriod;
        this.status = RegistrationStatus.REGISTERED;
        this.active = true;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Class getClassEntity() {
        return classEntity;
    }

    public void setClassEntity(Class classEntity) {
        this.classEntity = classEntity;
    }

    public RegistrationPeriod getRegistrationPeriod() {
        return registrationPeriod;
    }

    public void setRegistrationPeriod(RegistrationPeriod registrationPeriod) {
        this.registrationPeriod = registrationPeriod;
    }

    public RegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(RegistrationStatus status) {
        this.status = status;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}

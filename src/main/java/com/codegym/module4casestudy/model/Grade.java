package com.codegym.module4casestudy.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "grades")
public class Grade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Sinh viên
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    // Môn học
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    // Lớp học (để dễ query)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private Class classEntity;

    // Điểm 15 phút (5%)
    @Column(name = "grade_15", precision = 4, scale = 2)
    private Double grade15;

    // Điểm giữa kỳ (20%)
    @Column(name = "grade_midterm", precision = 4, scale = 2)
    private Double gradeMidterm;

    // Điểm chuyên cần (5%)
    @Column(name = "grade_attendance", precision = 4, scale = 2)
    private Double gradeAttendance;

    // Điểm cuối kỳ (70%)
    @Column(name = "grade_final", precision = 4, scale = 2)
    private Double gradeFinal;

    // Điểm trung bình (tự động tính)
    @Column(name = "average_grade", precision = 4, scale = 2)
    private Double averageGrade;

    // Xếp hạng (A, B, C, D, F)
    @Column(name = "grade_rank", length = 2)
    private String gradeRank;

    // Học kỳ
    @Column(name = "semester", length = 20)
    private String semester;

    // Năm học
    @Column(name = "academic_year", length = 10)
    private String academicYear;

    // Ghi chú
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Ai nhập điểm
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    // Thời gian tạo
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Thời gian cập nhật
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Trạng thái
    @Column(name = "active", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean active = true;

    // Constructors
    public Grade() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.active = true;
    }

    public Grade(User student, Subject subject, Class classEntity) {
        this();
        this.student = student;
        this.subject = subject;
        this.classEntity = classEntity;
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

    public Double getGrade15() {
        return grade15;
    }

    public void setGrade15(Double grade15) {
        this.grade15 = grade15;
    }

    public Double getGradeMidterm() {
        return gradeMidterm;
    }

    public void setGradeMidterm(Double gradeMidterm) {
        this.gradeMidterm = gradeMidterm;
    }

    public Double getGradeAttendance() {
        return gradeAttendance;
    }

    public void setGradeAttendance(Double gradeAttendance) {
        this.gradeAttendance = gradeAttendance;
    }

    public Double getGradeFinal() {
        return gradeFinal;
    }

    public void setGradeFinal(Double gradeFinal) {
        this.gradeFinal = gradeFinal;
    }

    public Double getAverageGrade() {
        return averageGrade;
    }

    public void setAverageGrade(Double averageGrade) {
        this.averageGrade = averageGrade;
    }

    public String getGradeRank() {
        return gradeRank;
    }

    public void setGradeRank(String gradeRank) {
        this.gradeRank = gradeRank;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
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
    public void calculateAverageGrade() {
        if (grade15 != null && gradeMidterm != null && gradeAttendance != null && gradeFinal != null) {
            // Tính theo tỷ lệ: 15 phút (5%) + Giữa kỳ (20%) + Chuyên cần (5%) + Cuối kỳ (70%)
            this.averageGrade = (grade15 * 0.05) + (gradeMidterm * 0.20) + (gradeAttendance * 0.05) + (gradeFinal * 0.70);
            this.gradeRank = calculateGradeRank(this.averageGrade);
        }
    }

    private String calculateGradeRank(Double grade) {
        if (grade >= 8.5) return "A";
        if (grade >= 7.0) return "B";
        if (grade >= 5.5) return "C";
        if (grade >= 4.0) return "D";
        return "F";
    }

    @Override
    public String toString() {
        return "Grade{" +
                "id=" + id +
                ", student=" + (student != null ? student.getFullName() : null) +
                ", subject=" + (subject != null ? subject.getName() : null) +
                ", averageGrade=" + averageGrade +
                ", gradeRank='" + gradeRank + '\'' +
                '}';
    }
}

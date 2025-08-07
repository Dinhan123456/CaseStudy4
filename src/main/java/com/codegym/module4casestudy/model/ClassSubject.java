package com.codegym.module4casestudy.model;

//Nhi thêm để quản lý thông tin giảng viên dạy môn gì trong lớp nào
import javax.persistence.*;

@Entity
@Table(name = "class_subject")
public class ClassSubject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "class_id", nullable = false)
    private Class classEntity;

    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    // Constructors
    public ClassSubject() {}

    public ClassSubject(Class classEntity, Subject subject, User teacher) {
        this.classEntity = classEntity;
        this.subject = subject;
        this.teacher = teacher;
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

    @Override
    public String toString() {
        return "ClassSubject{" +
                "id=" + id +
                ", classEntity=" + (classEntity != null ? classEntity.getName() : null) +
                ", subject=" + (subject != null ? subject.getName() : null) +
                ", teacher=" + (teacher != null ? teacher.getFullName() : null) +
                '}';
    }
}

package com.codegym.module4casestudy.model;

//Nhi sửa để khớp với database hơn
import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "classes")
public class Class {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "status", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean active = true;

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }


    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Many-to-Many với User (Students)
    @ManyToMany
    @JoinTable(
        name = "student_class",
        joinColumns = @JoinColumn(name = "class_id"),
        inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private Set<User> students = new HashSet<>();

    // Many-to-Many với User (Teachers)
    @ManyToMany
    @JoinTable(
        name = "teacher_class",
        joinColumns = @JoinColumn(name = "class_id"),
        inverseJoinColumns = @JoinColumn(name = "teacher_id")
    )
    private Set<User> teachers = new HashSet<>();

    // Constructors
    public Class() {}

    public Class(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<User> getStudents() {
        return students;
    }

    public void setStudents(Set<User> students) {
        this.students = students;
    }

    public Set<User> getTeachers() {
        return teachers;
    }

    public void setTeachers(Set<User> teachers) {
        this.teachers = teachers;
    }

    @Override
    public String toString() {
        return "Class{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

}

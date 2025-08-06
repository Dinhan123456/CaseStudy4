package com.codegym.module4casestudy.model;

//Nhi thêm lớp này để quản lí môn học chứa các thuoc tính
//Hỗ trợ soft delete (đánh dấu active = false thay vì xóa hoàn toàn)

import javax.persistence.*;

@Entity
@Table(name = "subjects")
public class Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "credits", nullable = false)
    private Integer credits;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "active", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean active = true;

    // Constructors
    public Subject() {}

    public Subject(String name, Integer credits, String description) {
        this.name = name;
        this.credits = credits;
        this.description = description;
        this.active = true;
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

    public Integer getCredits() {
        return credits;
    }

    public void setCredits(Integer credits) {
        this.credits = credits;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "Subject{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", credits=" + credits +
                ", description='" + description + '\'' +
                ", active=" + active +
                '}';
    }
}

package com.codegym.module4casestudy.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class ClassDto {
    private Long id;

    @NotBlank(message = "Tên lớp không được để trống")
    private String className;

    @NotBlank(message = "Mô tả không được để trống")
    private String description;

    @NotNull(message = "Số học sinh tối đa không được để trống")
    @Min(value = 1, message = "Số học sinh tối đa phải lớn hơn 0")
    private Integer maxStudents;

    private boolean active = true;

    public ClassDto() {
    }

    public ClassDto(String className, String description, Integer maxStudents) {
        this.className = className;
        this.description = description;
        this.maxStudents = maxStudents;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getMaxStudents() {
        return maxStudents;
    }

    public void setMaxStudents(Integer maxStudents) {
        this.maxStudents = maxStudents;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
} 
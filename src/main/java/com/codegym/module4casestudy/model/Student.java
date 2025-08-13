 package com.codegym.module4casestudy.model;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

 @Entity
 @Table(name = "students")
 public class Student {
     @Id
     private Long id;
     @MapsId
     @OneToOne
     @JoinColumn(name = "id")
     private User user;


     @Column(name = "full_name")
     private String fullName;

     private String email;
     private String phone;


     @Column(name = "student_code", unique = true)
     private String studentCode;


     private LocalDate dateOfBirth;
     private String address;

     @Enumerated(EnumType.STRING)
     private Gender gender;

    @ManyToOne
    @JoinColumn(name = "class_id")
    private Class homeClass; // lớp chủ quản

    private Boolean active = true;

    // Many-to-Many với Classes (enrollment)
    @ManyToMany
    @JoinTable(
        name = "student_class",
        joinColumns = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "class_id")
    )
    private Set<Class> classes = new HashSet<>();

    // Enum cho giới tính (khớp với DB: M, F)
    public enum Gender { M, F }

     public Student() {
     }

     public Student(String studentCode, String fullName, String email, LocalDate dateOfBirth, Gender gender) {
         this.studentCode = studentCode;
         this.fullName = fullName;
         this.email = email;
         this.dateOfBirth = dateOfBirth;
         this.gender = gender;
     }

     public Long getId() {
         return id;
     }

     public void setId(Long id) {
         this.id = id;
     }

     public User getUser() {
         return user;
     }

     public void setUser(User user) {
         this.user = user;
     }

     public String getFullName() {
         return fullName;
     }

     public void setFullName(String fullName) {
         this.fullName = fullName;
     }

     public String getEmail() {
         return email;
     }

     public void setEmail(String email) {
         this.email = email;
     }

     public String getPhone() {
         return phone;
     }

     public void setPhone(String phone) {
         this.phone = phone;
     }

     public String getStudentCode() {
         return studentCode;
     }

     public void setStudentCode(String studentCode) {
         this.studentCode = studentCode;
     }

     public LocalDate getDateOfBirth() {
         return dateOfBirth;
     }

     public void setDateOfBirth(LocalDate dateOfBirth) {
         this.dateOfBirth = dateOfBirth;
     }

     public String getAddress() {
         return address;
     }

     public void setAddress(String address) {
         this.address = address;
     }

     public Gender getGender() {
         return gender;
     }

     public void setGender(Gender gender) {
         this.gender = gender;
     }

    public Class getHomeClass() {
        return homeClass;
    }

    public void setHomeClass(Class homeClass) {
        this.homeClass = homeClass;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Set<Class> getClasses() {
        return classes;
    }

    public void setClasses(Set<Class> classes) {
        this.classes = classes;
    }
}
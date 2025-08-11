package com.codegym.module4casestudy.repository;

import com.codegym.module4casestudy.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    
    Optional<Student> findByStudentCode(String studentCode);
    
    Optional<Student> findByEmail(String email);
    
    List<Student> findByActive(boolean active);
    
    @Query("SELECT s FROM Student s WHERE s.class_.id = :classId")
    List<Student> findByClassId(@Param("classId") Long classId);
    
    @Query("SELECT s FROM Student s WHERE s.studentCode LIKE %:keyword% OR s.fullName LIKE %:keyword% OR s.email LIKE %:keyword%")
    List<Student> searchByKeyword(@Param("keyword") String keyword);
    
    boolean existsByStudentCode(String studentCode);
    
    boolean existsByStudentCodeAndIdNot(String studentCode, Long id);
    
    boolean existsByEmail(String email);
    
    boolean existsByEmailAndIdNot(String email, Long id);
} 
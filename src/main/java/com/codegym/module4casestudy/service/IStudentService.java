package com.codegym.module4casestudy.service;

import com.codegym.module4casestudy.dto.StudentDto;
import com.codegym.module4casestudy.model.Student;

import java.util.List;
import java.util.Optional;

public interface IStudentService {
    
    List<Student> findAll();
    
    List<Student> findAllActive();
    
    Optional<Student> findById(Long id);
    
    Optional<Student> findByStudentCode(String studentCode);
    
    Optional<Student> findByEmail(String email);
    
    List<Student> findByClassId(Long classId);
    
    List<Student> searchByKeyword(String keyword);
    
    Student save(StudentDto studentDto);
    
    Student update(Long id, StudentDto studentDto);
    
    void deleteById(Long id);
    
    boolean existsByStudentCode(String studentCode);
    
    boolean existsByStudentCodeAndIdNot(String studentCode, Long id);
    
    boolean existsByEmail(String email);
    
    boolean existsByEmailAndIdNot(String email, Long id);
} 
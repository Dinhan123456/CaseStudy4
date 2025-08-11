package com.codegym.module4casestudy.service;

import com.codegym.module4casestudy.model.StudentRegistration;

import java.util.List;
import java.util.Optional;

public interface IStudentRegistrationService {
    
    // CRUD cơ bản
    List<StudentRegistration> findAll();
    
    List<StudentRegistration> findAllActive();
    
    Optional<StudentRegistration> findById(Long id);
    
    StudentRegistration save(StudentRegistration registration);
    
    StudentRegistration update(Long id, StudentRegistration registration);
    
    void deleteById(Long id);
    
    // Tìm kiếm theo các tiêu chí
    List<StudentRegistration> findByStudentId(Long studentId);
    
    List<StudentRegistration> findByScheduleId(Long scheduleId);
    
    List<StudentRegistration> findBySubjectId(Long subjectId);
    
    List<StudentRegistration> findByClassId(Long classId);
    
    List<StudentRegistration> findByRegistrationPeriodId(Long periodId);
    
    List<StudentRegistration> findByStatus(StudentRegistration.RegistrationStatus status);
    
    // Kiểm tra đăng ký
    boolean existsByStudentIdAndScheduleId(Long studentId, Long scheduleId);
    
    boolean existsByStudentIdAndSubjectId(Long studentId, Long subjectId);
    
    // Đăng ký môn học
    StudentRegistration registerStudentToSubject(Long studentId, Long subjectId, Long classId, String notes);
    
    StudentRegistration registerStudentToSchedule(Long studentId, Long scheduleId, String notes);
    
    // Hủy đăng ký
    StudentRegistration cancelRegistration(Long registrationId, String reason);
    
    // Xác nhận đăng ký
    StudentRegistration confirmRegistration(Long registrationId);
    
    // Thống kê
    long countBySubjectId(Long subjectId);
    
    long countByClassId(Long classId);
    
    long countByStatus(StudentRegistration.RegistrationStatus status);
}

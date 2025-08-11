package com.codegym.module4casestudy.service;

import com.codegym.module4casestudy.model.Grade;

import java.util.List;
import java.util.Optional;

public interface IGradeService {
    
    // CRUD cơ bản
    List<Grade> findAll();
    
    List<Grade> findAllActive();
    
    Optional<Grade> findById(Long id);
    
    Grade save(Grade grade);
    
    Grade update(Long id, Grade grade);
    
    void deleteById(Long id);
    
    // Tìm kiếm theo các tiêu chí
    List<Grade> findByStudentId(Long studentId);
    
    List<Grade> findBySubjectId(Long subjectId);
    
    List<Grade> findByClassId(Long classId);
    
    Optional<Grade> findByStudentIdAndSubjectId(Long studentId, Long subjectId);
    
    List<Grade> findByClassIdAndSubjectId(Long classId, Long subjectId);
    
    List<Grade> findBySemesterAndAcademicYear(String semester, String academicYear);
    
    List<Grade> findByGradeRank(String gradeRank);
    
    List<Grade> searchByKeyword(String keyword);
    
    // Thống kê
    List<Object[]> getGradeStatisticsByClass(Long classId);
    
    List<Object[]> getGradeStatisticsBySubject(Long subjectId);
    
    Double getAverageGradeByClass(Long classId);
    
    Double getAverageGradeBySubject(Long subjectId);
    
    // Kiểm tra tồn tại
    boolean existsByStudentIdAndSubjectId(Long studentId, Long subjectId);
    
    // Tạo điểm mới
    Grade createGrade(Long studentId, Long subjectId, Long classId, 
                     Double grade15, Double gradeMidterm, Double gradeAttendance, Double gradeFinal,
                     String semester, String academicYear, String notes, Long createdById);
    
    // Cập nhật điểm
    Grade updateGrade(Long gradeId, Double grade15, Double gradeMidterm, 
                     Double gradeAttendance, Double gradeFinal, String notes);
    
    // Lưu điểm (tạo mới hoặc cập nhật)
    Grade saveGrade(Long studentId, Long subjectId, Long classId, 
                   Double midtermScore, Double finalScore, Double attendanceScore, 
                   Double assignmentScore, String notes);
}

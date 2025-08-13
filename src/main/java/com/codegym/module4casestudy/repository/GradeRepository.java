package com.codegym.module4casestudy.repository;

import com.codegym.module4casestudy.model.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
    
    // Tìm điểm theo sinh viên
    List<Grade> findByStudentIdAndActiveTrue(Long studentId);
    
    // Tìm điểm theo môn học
    List<Grade> findBySubjectIdAndActiveTrue(Long subjectId);
    
    // Tìm điểm theo lớp học
    List<Grade> findByClassEntityIdAndActiveTrue(Long classId);
    
    // Tìm điểm theo sinh viên và môn học
    Optional<Grade> findByStudentIdAndSubjectIdAndActiveTrue(Long studentId, Long subjectId);
    
    // Tìm điểm theo lớp và môn học
    List<Grade> findByClassEntityIdAndSubjectIdAndActiveTrue(Long classId, Long subjectId);
    
    // Tìm điểm theo học kỳ và năm học
    List<Grade> findBySemesterAndAcademicYearAndActiveTrue(String semester, String academicYear);
    
    // Tìm điểm theo xếp hạng
    List<Grade> findByGradeRankAndActiveTrue(String gradeRank);
    
    // Tìm điểm theo người tạo
    List<Grade> findByCreatedByIdAndActiveTrue(Long createdById);
    
    // Tìm kiếm điểm theo tên sinh viên hoặc tên môn học
    @Query("SELECT g FROM Grade g WHERE g.active = true AND " +
           "(g.student.fullName LIKE %:keyword% OR g.subject.name LIKE %:keyword%)")
    List<Grade> searchByKeyword(@Param("keyword") String keyword);
    
    // Thống kê điểm theo lớp
    @Query("SELECT g.gradeRank, COUNT(g) FROM Grade g WHERE g.classEntity.id = :classId AND g.active = true GROUP BY g.gradeRank")
    List<Object[]> getGradeStatisticsByClass(@Param("classId") Long classId);
    
    // Thống kê điểm theo môn học
    @Query("SELECT g.gradeRank, COUNT(g) FROM Grade g WHERE g.subject.id = :subjectId AND g.active = true GROUP BY g.gradeRank")
    List<Object[]> getGradeStatisticsBySubject(@Param("subjectId") Long subjectId);
    
    // Lấy điểm trung bình theo lớp
    @Query("SELECT AVG(g.averageGrade) FROM Grade g WHERE g.classEntity.id = :classId AND g.active = true")
    Double getAverageGradeByClass(@Param("classId") Long classId);
    
    // Lấy điểm trung bình theo môn học
    @Query("SELECT AVG(g.averageGrade) FROM Grade g WHERE g.subject.id = :subjectId AND g.active = true")
    Double getAverageGradeBySubject(@Param("subjectId") Long subjectId);
    
    // Kiểm tra xem sinh viên đã có điểm cho môn học này chưa
    boolean existsByStudentIdAndSubjectIdAndActiveTrue(Long studentId, Long subjectId);
}

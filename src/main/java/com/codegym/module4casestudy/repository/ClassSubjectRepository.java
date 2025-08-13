package com.codegym.module4casestudy.repository;


import com.codegym.module4casestudy.model.ClassSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClassSubjectRepository extends JpaRepository<ClassSubject, Long> {
//Nhi têm để query phức tạp để lấy dữ liệu với relationships

    // Lấy tất cả môn học và giảng viên của một lớp
    @Query("SELECT cs FROM ClassSubject cs JOIN FETCH cs.subject JOIN FETCH cs.teacher WHERE cs.classEntity.id = :classId")
    List<ClassSubject> findByClassIdWithSubjectAndTeacher(@Param("classId") Long classId);

    // Tìm môn học theo lớp
    List<ClassSubject> findByClassEntityId(Long classId);

    // Tìm lớp học theo giảng viên
    List<ClassSubject> findByTeacherId(Long teacherId);

    // Tìm môn học theo giảng viên
    List<ClassSubject> findBySubjectId(Long subjectId);

    // Kiểm tra xem giảng viên đã dạy môn này trong lớp này chưa
    boolean existsByClassEntityIdAndSubjectIdAndTeacherId(Long classId, Long subjectId, Long teacherId);

    // Xóa phân công theo lớp, môn và giảng viên
    void deleteByClassEntityIdAndSubjectIdAndTeacherId(Long classId, Long subjectId, Long teacherId);
}

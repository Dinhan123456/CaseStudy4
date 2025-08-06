package com.codegym.module4casestudy.repository;

//Nhi thêm lớp này để quản lí môn học : tìm, lấy, kiểm tra

import com.codegym.module4casestudy.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    // Tìm kiếm môn học theo tên
    List<Subject> findByNameContainingIgnoreCase(String name);

    // Lấy tất cả môn học đang hoạt động
    List<Subject> findByActiveTrue();

    // Tìm kiếm môn học đang hoạt động theo tên
    @Query("SELECT s FROM Subject s WHERE s.active = true AND s.name LIKE %:keyword%")
    List<Subject> findActiveSubjectsByKeyword(@Param("keyword") String keyword);

    // Kiểm tra tên môn học đã tồn tại chưa
    boolean existsByNameIgnoreCase(String name);
}

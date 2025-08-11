package com.codegym.module4casestudy.repository;

import com.codegym.module4casestudy.model.Class;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClassRepository extends JpaRepository<Class, Long> {


    Optional<Class> findByName(String name);

    List<Class> findByActive(boolean active);
    
    @Query("SELECT c FROM Class c WHERE c.name LIKE %:keyword% OR c.description LIKE %:keyword%")
    List<Class> searchByKeyword(@Param("keyword") String keyword);
    
    boolean existsByName(String className);
    
    @Query("SELECT COUNT(c) > 0 FROM Class c WHERE c.name = :className AND c.id != :id")
    boolean existsByNameAndIdNot(@Param("className") String className, @Param("id") Long id);

    //Khúc này Nhi thêm để  tạo các query phức tạp cho chúng lấy dữ liệu với relationships

    // Tìm kiếm lớp học theo tên
    List<Class> findByNameContainingIgnoreCase(String name);

    // Lấy tất cả lớp học cùng với students và teachers
    @Query("SELECT DISTINCT c FROM Class c LEFT JOIN FETCH c.students LEFT JOIN FETCH c.teachers")
    List<Class> findAllWithStudentsAndTeachers();

    // Lấy lớp học theo ID cùng với students và teachers
    @Query("SELECT c FROM Class c LEFT JOIN FETCH c.students LEFT JOIN FETCH c.teachers WHERE c.id = :id")
    Class findByIdWithStudentsAndTeachers(@Param("id") Long id);

    // Kiểm tra tên lớp học đã tồn tại chưa
    boolean existsByNameIgnoreCase(String name);

    // Tìm lớp học có sinh viên cụ thể
    @Query("SELECT c FROM Class c JOIN c.students s WHERE s.id = :studentId")
    List<Class> findClassesByStudentId(@Param("studentId") Long studentId);

    // Tìm lớp học có giảng viên cụ thể
    @Query("SELECT c FROM Class c JOIN c.teachers t WHERE t.id = :teacherId")
    List<Class> findClassesByTeacherId(@Param("teacherId") Long teacherId);

    // Dành cho dashboard: fetch sẵn students/teachers để khỏi lazy
    @Query("""
       SELECT DISTINCT c
       FROM Class c
       JOIN c.students s
       LEFT JOIN FETCH c.students
       LEFT JOIN FETCH c.teachers
       WHERE s.id = :studentId
       ORDER BY c.id DESC
    """)
    List<Class> findRecentByStudentIdWithFetch(@Param("studentId") Long studentId);

    // Lớp SV đã đăng ký
    @Query("""
       SELECT DISTINCT c
       FROM Class c
       JOIN c.students s
       WHERE s.id = :studentId
       ORDER BY c.id DESC
    """)
    List<Class> findByStudentId(@Param("studentId") Long studentId);

}

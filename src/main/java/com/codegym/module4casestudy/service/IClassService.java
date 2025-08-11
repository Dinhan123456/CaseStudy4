package com.codegym.module4casestudy.service;

import com.codegym.module4casestudy.model.Class;
import com.codegym.module4casestudy.model.ClassSubject;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

//Nhi add thêm các phương thức để implement
public interface IClassService {
    List<Class> findAll();

    List<Class> findAllWithStudentsAndTeachers();

    Optional<Class> findById(Long id);

    Class findByIdWithStudentsAndTeachers(Long id);

    // Method save nhận tham số là Class entity
    Class save(Class classEntity);

    void deleteById(Long id);

    List<Class> searchByKeyword(String keyword);

    // Method kiểm tra tên lớp đã tồn tại
    boolean existsByName(String name);

    // Method kiểm tra tên lớp đã tồn tại (trừ ID hiện tại)
    boolean existsByNameAndIdNot(String name, Long id);

    // Quản lý sinh viên trong lớp
    void addStudentToClass(Long classId, Long studentId);

    void removeStudentFromClass(Long classId, Long studentId);

    // Quản lý giảng viên trong lớp
    void addTeacherToClass(Long classId, Long teacherId);

    void removeTeacherFromClass(Long classId, Long teacherId);

    // Quản lý môn học và giảng viên dạy
    List<ClassSubject> getClassSubjects(Long classId);

    void assignTeacherToSubject(Long classId, Long subjectId, Long teacherId);

    void removeTeacherFromSubject(Long classId, Long subjectId, Long teacherId);

    // Tìm lớp học theo student/teacher
    List<Class> findClassesByStudentId(Long studentId);

    List<Class> findClassesByTeacherId(Long teacherId);

    List<Class> findRecentClassesForStudentWithFetch(Long studentId);


}

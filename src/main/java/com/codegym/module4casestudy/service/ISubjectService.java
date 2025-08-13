package com.codegym.module4casestudy.service;
//Nhi thêm lớp này để quản lí môn học : chứa các phương thức cho lớp SubjectServiceImpl

import com.codegym.module4casestudy.model.Subject;

import java.util.List;
import java.util.Optional;

public interface ISubjectService {
    List<Subject> findAll();
    List<Subject> findActiveSubjects();
    Optional<Subject> findById(Long id);
    Subject save(Subject subject);
    void deleteById(Long id);
    List<Subject> searchByKeyword(String keyword);
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);
}

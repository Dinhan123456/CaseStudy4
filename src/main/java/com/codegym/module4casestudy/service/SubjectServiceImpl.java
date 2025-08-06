package com.codegym.module4casestudy.service;
//Nhi thêm lớp này để quản lí môn học:
//CRUD operations hoàn chỉnh
//Tìm kiếm với keyword
//Validation tên môn học

import com.codegym.module4casestudy.model.Subject;
import com.codegym.module4casestudy.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SubjectServiceImpl implements ISubjectService {

    @Autowired
    private SubjectRepository subjectRepository;

    @Override
    public List<Subject> findAll() {
        return subjectRepository.findAll();
    }

    @Override
    public List<Subject> findActiveSubjects() {
        return subjectRepository.findByActiveTrue();
    }

    @Override
    public Optional<Subject> findById(Long id) {
        return subjectRepository.findById(id);
    }

    @Override
    public Subject save(Subject subject) {
        return subjectRepository.save(subject);
    }

    @Override
    public void deleteById(Long id) {
        Optional<Subject> subject = subjectRepository.findById(id);
        if (subject.isPresent()) {
            // Soft delete - chỉ đánh dấu là không hoạt động
            Subject subjectToDelete = subject.get();
            subjectToDelete.setActive(false);
            subjectRepository.save(subjectToDelete);
        }
    }

    @Override
    public List<Subject> searchByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findActiveSubjects();
        }
        return subjectRepository.findActiveSubjectsByKeyword(keyword);
    }

    @Override
    public boolean existsByName(String name) {
        return subjectRepository.existsByNameIgnoreCase(name);
    }

    @Override
    public boolean existsByNameAndIdNot(String name, Long id) {
        List<Subject> subjects = subjectRepository.findByNameContainingIgnoreCase(name);
        return subjects.stream().anyMatch(s -> s.getName().equalsIgnoreCase(name) && !s.getId().equals(id));
    }
}

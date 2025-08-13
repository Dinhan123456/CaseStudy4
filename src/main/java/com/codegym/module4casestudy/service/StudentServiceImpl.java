package com.codegym.module4casestudy.service;

import com.codegym.module4casestudy.dto.StudentDto;
import com.codegym.module4casestudy.model.Class;
import com.codegym.module4casestudy.model.Student;
import com.codegym.module4casestudy.repository.ClassRepository;
import com.codegym.module4casestudy.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudentServiceImpl implements IStudentService {
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private ClassRepository classRepository;
    
    @Override
    public List<Student> findAll() {
        return studentRepository.findAll();
    }
    
    @Override
    public List<Student> findAllActive() {
        return studentRepository.findByActive(true);
    }
    
    @Override
    public Optional<Student> findById(Long id) {
        return studentRepository.findById(id);
    }
    
    @Override
    public Optional<Student> findByStudentCode(String studentCode) {
        return studentRepository.findByStudentCode(studentCode);
    }
    
    @Override
    public Optional<Student> findByEmail(String email) {
        return studentRepository.findByEmail(email);
    }
    
    @Override
    public List<Student> findByClassId(Long classId) {
        return studentRepository.findByClassId(classId);
    }
    
    @Override
    public List<Student> searchByKeyword(String keyword) {
        return studentRepository.searchByKeyword(keyword);
    }
    
    @Override
    public Student save(StudentDto studentDto) {
        Student student = new Student();
        student.setStudentCode(studentDto.getStudentCode());
        student.setFullName(studentDto.getFullName());
        student.setEmail(studentDto.getEmail());
        student.setPhone(studentDto.getPhone());
        student.setDateOfBirth(studentDto.getDateOfBirth());
        student.setAddress(studentDto.getAddress());
        student.setGender(studentDto.getGender());
        student.setActive(studentDto.isActive());
        
        if (studentDto.getClassId() != null) {
            Optional<Class> class_ = classRepository.findById(studentDto.getClassId());
            class_.ifPresent(student::setHomeClass);
        }
        
        return studentRepository.save(student);
    }
    
    @Override
    public Student update(Long id, StudentDto studentDto) {
        Optional<Student> existingStudent = studentRepository.findById(id);
        if (existingStudent.isPresent()) {
            Student student = existingStudent.get();
            student.setStudentCode(studentDto.getStudentCode());
            student.setFullName(studentDto.getFullName());
            student.setEmail(studentDto.getEmail());
            student.setPhone(studentDto.getPhone());
            student.setDateOfBirth(studentDto.getDateOfBirth());
            student.setAddress(studentDto.getAddress());
            student.setGender(studentDto.getGender());
            student.setActive(studentDto.isActive());
            
            if (studentDto.getClassId() != null) {
                Optional<Class> class_ = classRepository.findById(studentDto.getClassId());
                class_.ifPresent(student::setHomeClass);
            }
            
            return studentRepository.save(student);
        }
        throw new RuntimeException("Student not found with id: " + id);
    }
    
    @Override
    public void deleteById(Long id) {
        studentRepository.deleteById(id);
    }
    
    @Override
    public boolean existsByStudentCode(String studentCode) {
        return studentRepository.existsByStudentCode(studentCode);
    }
    
    @Override
    public boolean existsByStudentCodeAndIdNot(String studentCode, Long id) {
        return studentRepository.existsByStudentCodeAndIdNot(studentCode, id);
    }
    
    @Override
    public boolean existsByEmail(String email) {
        return studentRepository.existsByEmail(email);
    }
    
    @Override
    public boolean existsByEmailAndIdNot(String email, Long id) {
        return studentRepository.existsByEmailAndIdNot(email, id);
    }
} 
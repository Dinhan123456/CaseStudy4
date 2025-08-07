package com.codegym.module4casestudy.controller;

import com.codegym.module4casestudy.dto.StudentDto;
import com.codegym.module4casestudy.model.Student;
import com.codegym.module4casestudy.service.IStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
public class StudentRestController {

    @Autowired
    private IStudentService studentService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<List<Student>> getAllStudents() {
        List<Student> students = studentService.findAllActive();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Student> getStudentById(@PathVariable Long id) {
        Optional<Student> student = studentService.findById(id);
        return student.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/class/{classId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<List<Student>> getStudentsByClass(@PathVariable Long classId) {
        List<Student> students = studentService.findByClassId(classId);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<List<Student>> searchStudents(@RequestParam String keyword) {
        List<Student> students = studentService.searchByKeyword(keyword);
        return ResponseEntity.ok(students);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Student> createStudent(@Validated @RequestBody StudentDto studentDto) {
        if (studentService.existsByStudentCode(studentDto.getStudentCode())) {
            return ResponseEntity.badRequest().build();
        }
        if (studentService.existsByEmail(studentDto.getEmail())) {
            return ResponseEntity.badRequest().build();
        }
        Student savedStudent = studentService.save(studentDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedStudent);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Student> updateStudent(@PathVariable Long id, @Validated @RequestBody StudentDto studentDto) {
        if (studentService.existsByStudentCodeAndIdNot(studentDto.getStudentCode(), id)) {
            return ResponseEntity.badRequest().build();
        }
        if (studentService.existsByEmailAndIdNot(studentDto.getEmail(), id)) {
            return ResponseEntity.badRequest().build();
        }
        try {
            Student updatedStudent = studentService.update(id, studentDto);
            return ResponseEntity.ok(updatedStudent);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        try {
            studentService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
} 
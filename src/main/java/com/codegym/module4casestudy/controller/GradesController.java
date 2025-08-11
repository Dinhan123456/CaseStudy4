package com.codegym.module4casestudy.controller;

import com.codegym.module4casestudy.model.Grade;
import com.codegym.module4casestudy.model.User;
import com.codegym.module4casestudy.service.IClassService;
import com.codegym.module4casestudy.service.IGradeService;
import com.codegym.module4casestudy.service.ISubjectService;
import com.codegym.module4casestudy.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class GradesController {

    @Autowired
    private IClassService classService;

    @Autowired
    private ISubjectService subjectService;
    
    @Autowired
    private IGradeService gradeService;
    
    @Autowired
    private IUserService userService;

    @GetMapping("/grades")
    public String viewAdminGrades(Model model) {
        // Truyền dữ liệu classes và subjects cho template
        model.addAttribute("classes", classService.findAll());
        model.addAttribute("subjects", subjectService.findActiveSubjects());

        return "admin/admin-grades"; // Tên file HTML trong templates/admin
    }
    

    // API để lấy danh sách Users với role STUDENT
    @GetMapping("/api/users/students")
    @ResponseBody
    public ResponseEntity<List<User>> getUserStudents() {
        List<User> students = userService.findByRole(com.codegym.module4casestudy.model.Role.STUDENT);
        return ResponseEntity.ok(students);
    }
    
    // API để lấy danh sách môn học
    @GetMapping("/api/subjects")
    @ResponseBody
    public ResponseEntity<List<com.codegym.module4casestudy.model.Subject>> getAllSubjects() {
        List<com.codegym.module4casestudy.model.Subject> subjects = subjectService.findActiveSubjects();
        return ResponseEntity.ok(subjects);
    }
    

    
    // API để lấy điểm theo lớp
    @GetMapping("/api/grades/class/{classId}")
    @ResponseBody
    public ResponseEntity<List<Grade>> getGradesByClass(@PathVariable Long classId) {
        List<Grade> grades = gradeService.findByClassId(classId);
        return ResponseEntity.ok(grades);
    }
    
    // API để lấy điểm theo môn học
    @GetMapping("/api/grades/subject/{subjectId}")
    @ResponseBody
    public ResponseEntity<List<Grade>> getGradesBySubject(@PathVariable Long subjectId) {
        List<Grade> grades = gradeService.findBySubjectId(subjectId);
        return ResponseEntity.ok(grades);
    }
    
    // API để tìm kiếm điểm
    @GetMapping("/api/grades/search")
    @ResponseBody
    public ResponseEntity<List<Grade>> searchGrades(@RequestParam String keyword) {
        List<Grade> grades = gradeService.searchByKeyword(keyword);
        return ResponseEntity.ok(grades);
    }
    
    // API để lấy tất cả điểm
    @GetMapping("/api/grades")
    @ResponseBody
    public ResponseEntity<List<Grade>> getAllGrades() {
        List<Grade> grades = gradeService.findAllActive();
        return ResponseEntity.ok(grades);
    }
    
    // API để lấy điểm theo ID
    @GetMapping("/api/grades/detail/{gradeId}")
    @ResponseBody
    public ResponseEntity<?> getGradeById(@PathVariable Long gradeId) {
        try {
            Optional<Grade> grade = gradeService.findById(gradeId);
            if (grade.isPresent()) {
                return ResponseEntity.ok(grade.get());
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Không tìm thấy điểm!");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // API để tạo điểm mới
    @PostMapping("/api/grades")
    @ResponseBody
    public ResponseEntity<?> createGrade(@RequestParam Long studentId,
                                       @RequestParam Long subjectId,
                                       @RequestParam(required = false) Long classId,
                                       @RequestParam(required = false) Double grade15,
                                       @RequestParam(required = false) Double gradeMidterm,
                                       @RequestParam(required = false) Double gradeAttendance,
                                       @RequestParam(required = false) Double gradeFinal,
                                       @RequestParam(required = false) String semester,
                                       @RequestParam(required = false) String academicYear,
                                       @RequestParam(required = false) String notes) {
        try {
            // Lấy thông tin người tạo
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = userService.findByUsername(auth.getName()).orElse(null);
            
            Grade grade = gradeService.createGrade(studentId, subjectId, classId,
                    grade15, gradeMidterm, gradeAttendance, gradeFinal,
                    semester, academicYear, notes, currentUser != null ? currentUser.getId() : null);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Tạo điểm thành công!");
            response.put("grade", grade);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // API để cập nhật điểm
    @PutMapping("/api/grades/{gradeId}")
    @ResponseBody
    public ResponseEntity<?> updateGrade(@PathVariable Long gradeId,
                                       @RequestParam(required = false) Double grade15,
                                       @RequestParam(required = false) Double gradeMidterm,
                                       @RequestParam(required = false) Double gradeAttendance,
                                       @RequestParam(required = false) Double gradeFinal,
                                       @RequestParam(required = false) String notes) {
        try {
            Grade grade = gradeService.updateGrade(gradeId, grade15, gradeMidterm, 
                    gradeAttendance, gradeFinal, notes);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cập nhật điểm thành công!");
            response.put("grade", grade);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // API để xóa điểm
    @DeleteMapping("/api/grades/{gradeId}")
    @ResponseBody
    public ResponseEntity<?> deleteGrade(@PathVariable Long gradeId) {
        try {
            gradeService.deleteById(gradeId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Xóa điểm thành công!");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // API để lấy thống kê điểm theo lớp
    @GetMapping("/api/grades/statistics/class/{classId}")
    @ResponseBody
    public ResponseEntity<?> getGradeStatisticsByClass(@PathVariable Long classId) {
        try {
            List<Object[]> statistics = gradeService.getGradeStatisticsByClass(classId);
            Double averageGrade = gradeService.getAverageGradeByClass(classId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", statistics);
            response.put("averageGrade", averageGrade);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // API để lấy thống kê điểm theo môn học
    @GetMapping("/api/grades/statistics/subject/{subjectId}")
    @ResponseBody
    public ResponseEntity<?> getGradeStatisticsBySubject(@PathVariable Long subjectId) {
        try {
            List<Object[]> statistics = gradeService.getGradeStatisticsBySubject(subjectId);
            Double averageGrade = gradeService.getAverageGradeBySubject(subjectId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", statistics);
            response.put("averageGrade", averageGrade);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

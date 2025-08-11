package com.codegym.module4casestudy.controller;

import com.codegym.module4casestudy.model.StudentRegistration;
import com.codegym.module4casestudy.service.IClassService;
import com.codegym.module4casestudy.service.IStudentRegistrationService;
import com.codegym.module4casestudy.service.ISubjectService;
import com.codegym.module4casestudy.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class StudentRegistrationController {

    @Autowired
    private IStudentRegistrationService registrationService;

    @Autowired
    private IUserService userService;

    @Autowired
    private ISubjectService subjectService;

    @Autowired
    private IClassService classService;

    // Trang quản lý đăng ký môn học
    @GetMapping("/admin/registrations")
    public String viewRegistrations(Model model) {
        model.addAttribute("students", userService.findByRole(com.codegym.module4casestudy.model.Role.STUDENT));
        model.addAttribute("subjects", subjectService.findActiveSubjects());
        model.addAttribute("classes", classService.findAll());
        return "admin/admin-registrations";
    }

    // API để lấy tất cả đăng ký
    @GetMapping("/api/registrations")
    @ResponseBody
    public ResponseEntity<List<StudentRegistration>> getAllRegistrations() {
        List<StudentRegistration> registrations = registrationService.findAllActive();
        return ResponseEntity.ok(registrations);
    }

    // API để lấy đăng ký theo sinh viên
    @GetMapping("/api/registrations/student/{studentId}")
    @ResponseBody
    public ResponseEntity<List<StudentRegistration>> getRegistrationsByStudent(@PathVariable Long studentId) {
        List<StudentRegistration> registrations = registrationService.findByStudentId(studentId);
        return ResponseEntity.ok(registrations);
    }

    // API để lấy đăng ký theo môn học
    @GetMapping("/api/registrations/subject/{subjectId}")
    @ResponseBody
    public ResponseEntity<List<StudentRegistration>> getRegistrationsBySubject(@PathVariable Long subjectId) {
        List<StudentRegistration> registrations = registrationService.findBySubjectId(subjectId);
        return ResponseEntity.ok(registrations);
    }

    // API để lấy đăng ký theo lớp học
    @GetMapping("/api/registrations/class/{classId}")
    @ResponseBody
    public ResponseEntity<List<StudentRegistration>> getRegistrationsByClass(@PathVariable Long classId) {
        List<StudentRegistration> registrations = registrationService.findByClassId(classId);
        return ResponseEntity.ok(registrations);
    }

    // API để đăng ký sinh viên vào môn học
    @PostMapping("/api/registrations")
    @ResponseBody
    public ResponseEntity<?> registerStudentToSubject(@RequestParam Long studentId,
                                                     @RequestParam Long subjectId,
                                                     @RequestParam(required = false) Long classId,
                                                     @RequestParam(required = false) String notes) {
        try {
            StudentRegistration registration = registrationService.registerStudentToSubject(
                    studentId, subjectId, classId, notes);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đăng ký môn học thành công!");
            response.put("registration", registration);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // API để xác nhận đăng ký
    @PutMapping("/api/registrations/{registrationId}/confirm")
    @ResponseBody
    public ResponseEntity<?> confirmRegistration(@PathVariable Long registrationId) {
        try {
            StudentRegistration registration = registrationService.confirmRegistration(registrationId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Xác nhận đăng ký thành công!");
            response.put("registration", registration);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // API để hủy đăng ký
    @PutMapping("/api/registrations/{registrationId}/cancel")
    @ResponseBody
    public ResponseEntity<?> cancelRegistration(@PathVariable Long registrationId,
                                               @RequestParam(required = false) String reason) {
        try {
            StudentRegistration registration = registrationService.cancelRegistration(registrationId, reason);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Hủy đăng ký thành công!");
            response.put("registration", registration);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // API để xóa đăng ký
    @DeleteMapping("/api/registrations/{registrationId}")
    @ResponseBody
    public ResponseEntity<?> deleteRegistration(@PathVariable Long registrationId) {
        try {
            registrationService.deleteById(registrationId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Xóa đăng ký thành công!");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // API để lấy thống kê đăng ký
    @GetMapping("/api/registrations/statistics")
    @ResponseBody
    public ResponseEntity<?> getRegistrationStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalRegistrations", registrationService.countByStatus(StudentRegistration.RegistrationStatus.REGISTERED));
            statistics.put("confirmedRegistrations", registrationService.countByStatus(StudentRegistration.RegistrationStatus.CONFIRMED));
            statistics.put("cancelledRegistrations", registrationService.countByStatus(StudentRegistration.RegistrationStatus.CANCELLED));
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", statistics);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

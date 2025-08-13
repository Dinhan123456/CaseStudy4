package com.codegym.module4casestudy.controller;

import com.codegym.module4casestudy.model.User;
import com.codegym.module4casestudy.model.Class;
import com.codegym.module4casestudy.model.Subject;
import com.codegym.module4casestudy.model.Grade;
import com.codegym.module4casestudy.service.IUserService;
import com.codegym.module4casestudy.service.IClassService;
import com.codegym.module4casestudy.service.ISubjectService;
import com.codegym.module4casestudy.service.IGradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/teacher")
public class TeacherController {

    @Autowired
    private IUserService userService;

    @Autowired
    private IClassService classService;

    @Autowired
    private ISubjectService subjectService;

    @Autowired
    private IGradeService gradeService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Helper method để lấy thông tin giảng viên hiện tại
    private User getCurrentTeacher() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            System.out.println("DEBUG: Authentication is null");
            return null;
        }
        
        String username = auth.getName();
        System.out.println("DEBUG: Current username: " + username);
        
        User teacher = userService.findByUsername(username).orElse(null);
        if (teacher == null) {
            System.out.println("DEBUG: Teacher not found for username: " + username);
        } else {
            System.out.println("DEBUG: Teacher found: " + teacher.getFullName() + " (ID: " + teacher.getId() + ")");
        }
        
        return teacher;
    }

    @GetMapping("/dashboard")
    public String showTeacherDashboard(Model model) {
        User teacher = getCurrentTeacher();
        if (teacher == null) {
            model.addAttribute("error", "Không tìm thấy thông tin giảng viên! Vui lòng đăng nhập lại.");
            return "redirect:/login";
        }

        // Lấy các lớp mà teacher phụ trách
        List<Class> teachingClasses = classService.findClassesByTeacherId(teacher.getId());
        model.addAttribute("teacher", teacher);
        model.addAttribute("totalClasses", teachingClasses.size());
        model.addAttribute("recentClasses", teachingClasses.size() > 3 ? teachingClasses.subList(0, 3) : teachingClasses);

        return "teacher/teacher-panel";
    }

    @GetMapping("/debug")
    public String debugTeacher(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User teacher = getCurrentTeacher();
        
        model.addAttribute("auth", auth);
        model.addAttribute("teacher", teacher);
        model.addAttribute("authName", auth != null ? auth.getName() : "null");
        model.addAttribute("authDetails", auth != null ? auth.getDetails() : "null");
        model.addAttribute("authAuthorities", auth != null ? auth.getAuthorities() : "null");
        
        return "teacher/teacher-debug";
    }

    @GetMapping("/profile")
    public String showTeacherProfile(Model model) {
        User teacher = getCurrentTeacher();
        if (teacher == null) {
            model.addAttribute("error", "Không tìm thấy thông tin giảng viên! Vui lòng đăng nhập lại.");
            // Thêm một teacher mẫu để template không bị lỗi
            teacher = new User();
            teacher.setUsername("Unknown");
            teacher.setFullName("Không xác định");
            teacher.setEmail("");
            teacher.setPhone("");
        }

        model.addAttribute("teacher", teacher);
        return "teacher/teacher-profile";
    }

    @PostMapping("/update-profile")
    public String updateTeacherProfile(
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String fullName,
            @RequestParam(required = false) String passwordOld,
            @RequestParam(required = false) String password,
            RedirectAttributes redirectAttributes) {

        try {
            User teacher = getCurrentTeacher();
            if (teacher == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin giảng viên!");
                return "redirect:/teacher/profile";
            }

            // Kiểm tra email đã tồn tại chưa (tạm thời bỏ qua check này)
            // TODO: Thêm method existsByEmail vào UserService

            // Cập nhật thông tin cơ bản
            teacher.setEmail(email);
            teacher.setPhone(phone);
            teacher.setFullName(fullName);

            // Xử lý đổi mật khẩu
            if (password != null && !password.trim().isEmpty()) {
                if (passwordOld == null || passwordOld.trim().isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Vui lòng nhập mật khẩu cũ!");
                    return "redirect:/teacher/profile";
                }

                // Kiểm tra mật khẩu cũ
                if (!passwordEncoder.matches(passwordOld, teacher.getPassword())) {
                    redirectAttributes.addFlashAttribute("error", "Mật khẩu cũ không đúng!");
                    return "redirect:/teacher/profile";
                }

                // Mã hóa và cập nhật mật khẩu mới
                teacher.setPassword(passwordEncoder.encode(password));
            }

            // Lưu thông tin
            userService.save(teacher);
            redirectAttributes.addFlashAttribute("message", "Cập nhật thông tin thành công!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi cập nhật thông tin!");
        }

        return "redirect:/teacher/profile";
    }

    @GetMapping("/classes")
    public String showTeacherClasses(Model model) {
        User teacher = getCurrentTeacher();
        if (teacher == null || teacher.getId() == null) {
            model.addAttribute("error", "Không tìm thấy thông tin giảng viên! Vui lòng đăng nhập lại.");
            // Tạo teacher mẫu để template không bị lỗi
            teacher = new User();
            teacher.setUsername("Unknown");
            teacher.setFullName("Không xác định");
            teacher.setEmail("");
            teacher.setPhone("");
            model.addAttribute("teacher", teacher);
            model.addAttribute("classes", new java.util.ArrayList<>());
            return "teacher/teacher-classes";
        }

        try {
            // Lấy tất cả lớp có students và teachers (tránh lazy loading exception)
            List<Class> allClasses = classService.findAllWithStudentsAndTeachers();
            // Lọc lại các lớp mà teacher phụ trách
            List<Class> teachingClasses = new java.util.ArrayList<>();
            for (Class clazz : allClasses) {
                if (clazz.getTeachers() != null) {
                    for (com.codegym.module4casestudy.model.User t : clazz.getTeachers()) {
                        if (t.getId() != null && t.getId().equals(teacher.getId())) {
                            teachingClasses.add(clazz);
                            break;
                        }
                    }
                }
            }
            model.addAttribute("teacher", teacher);
            model.addAttribute("classes", teachingClasses);
        } catch (Exception e) {
            System.out.println("DEBUG: Exception in showTeacherClasses: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Có lỗi xảy ra khi tải danh sách lớp học: " + e.getMessage());
            model.addAttribute("classes", new java.util.ArrayList<>());
        }

        return "teacher/teacher-classes";
    }

    @GetMapping("/classes/{classId}")
    public String showClassDetails(@PathVariable Long classId, Model model) {
        User teacher = getCurrentTeacher();
        if (teacher == null) {
            return "redirect:/login";
        }

        Class teachingClass = classService.findById(classId).orElse(null);
        if (teachingClass == null) {
            model.addAttribute("error", "Không tìm thấy lớp học!");
            return "redirect:/teacher/classes";
        }

        // Kiểm tra xem teacher có phụ trách lớp này không
        List<Class> teachingClasses = classService.findClassesByTeacherId(teacher.getId());
        boolean isTeaching = teachingClasses.stream().anyMatch(c -> c.getId().equals(classId));

        if (!isTeaching) {
            model.addAttribute("error", "Bạn không phụ trách lớp này!");
            return "redirect:/teacher/classes";
        }

        model.addAttribute("teacher", teacher);
        // Đổi tên attribute tránh xung đột với SpEL keyword 'class'
        model.addAttribute("clazz", teachingClass);
        // Tạm thời comment out students vì chưa có relationship
        // model.addAttribute("students", teachingClass.getStudents());

        return "teacher/teacher-class-details";
    }

    @GetMapping("/grades")
    public String showGradeManagement(Model model) {
        User teacher = getCurrentTeacher();
        if (teacher == null) {
            return "redirect:/login";
        }

        // Lấy các lớp mà teacher phụ trách
        List<Class> teachingClasses = classService.findClassesByTeacherId(teacher.getId());
        List<Subject> subjects = subjectService.findActiveSubjects();

        model.addAttribute("teacher", teacher);
        model.addAttribute("classes", teachingClasses);
        model.addAttribute("subjects", subjects);

        return "teacher/teacher-grades";
    }

    // API để lấy điểm của sinh viên theo lớp
    @GetMapping("/api/grades/class/{classId}")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> getGradesByClass(@PathVariable Long classId) {
        try {
            User teacher = getCurrentTeacher();
            if (teacher == null) {
                return org.springframework.http.ResponseEntity.badRequest().body("Không tìm thấy thông tin giảng viên!");
            }

            // Kiểm tra teacher có phụ trách lớp này không
            List<Class> teachingClasses = classService.findClassesByTeacherId(teacher.getId());
            boolean isTeaching = teachingClasses.stream().anyMatch(c -> c.getId().equals(classId));
            
            if (!isTeaching) {
                return org.springframework.http.ResponseEntity.badRequest().body("Bạn không phụ trách lớp này!");
            }

            // Lấy danh sách điểm theo lớp
            List<Grade> grades = gradeService.findByClassId(classId);
            
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", true);
            response.put("grades", grades);
            
            return org.springframework.http.ResponseEntity.ok(response);
        } catch (Exception e) {
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return org.springframework.http.ResponseEntity.badRequest().body(response);
        }
    }

    // API để thêm/cập nhật điểm
    @PostMapping("/api/grades")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> saveGrade(@RequestParam Long studentId, 
                            @RequestParam Long subjectId,
                            @RequestParam(required = false) Long classId,
                            @RequestParam Double midtermScore,
                            @RequestParam Double finalScore,
                            @RequestParam Double attendanceScore,
                            @RequestParam Double assignmentScore,
                            @RequestParam(required = false) String notes) {
        try {
            User teacher = getCurrentTeacher();
            if (teacher == null) {
                java.util.Map<String, Object> response = new java.util.HashMap<>();
                response.put("success", false);
                response.put("message", "Không tìm thấy thông tin giảng viên!");
                return org.springframework.http.ResponseEntity.badRequest().body(response);
            }

            // Tạo hoặc cập nhật điểm
            Grade grade = gradeService.saveGrade(studentId, subjectId, classId, 
                midtermScore, finalScore, attendanceScore, assignmentScore, notes);
            
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", true);
            response.put("message", "Lưu điểm thành công!");
            response.put("grade", grade);
            
            return org.springframework.http.ResponseEntity.ok(response);
        } catch (Exception e) {
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return org.springframework.http.ResponseEntity.badRequest().body(response);
        }
    }

    // API để xóa điểm
    @PostMapping("/api/grades/{gradeId}/delete")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> deleteGrade(@PathVariable Long gradeId) {
        try {
            User teacher = getCurrentTeacher();
            if (teacher == null) {
                java.util.Map<String, Object> response = new java.util.HashMap<>();
                response.put("success", false);
                response.put("message", "Không tìm thấy thông tin giảng viên!");
                return org.springframework.http.ResponseEntity.badRequest().body(response);
            }

            gradeService.deleteById(gradeId);
            
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", true);
            response.put("message", "Xóa điểm thành công!");
            
            return org.springframework.http.ResponseEntity.ok(response);
        } catch (Exception e) {
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return org.springframework.http.ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/schedule")
    public String showTeacherSchedule(Model model) {
        User teacher = getCurrentTeacher();
        if (teacher == null) {
            return "redirect:/login";
        }

        // Lấy các lớp mà teacher phụ trách
        List<Class> teachingClasses = classService.findClassesByTeacherId(teacher.getId());
        model.addAttribute("teacher", teacher);
        model.addAttribute("classes", teachingClasses);

        return "teacher/teacher-schedule";
    }

    // Tạm thời comment out các method add/remove student vì cần model relationship hoàn chỉnh
    /*
    @PostMapping("/classes/{classId}/add-student")
    public String addStudentToClass(
            @PathVariable Long classId,
            @RequestParam Long studentId,
            RedirectAttributes redirectAttributes) {

        User teacher = getCurrentTeacher();
        if (teacher == null) {
            return "redirect:/login";
        }

        // TODO: Implement after fixing model relationships
        redirectAttributes.addFlashAttribute("message", "Tính năng đang phát triển!");
        return "redirect:/teacher/classes/" + classId;
    }

    @PostMapping("/classes/{classId}/remove-student")
    public String removeStudentFromClass(
            @PathVariable Long classId,
            @RequestParam Long studentId,
            RedirectAttributes redirectAttributes) {

        User teacher = getCurrentTeacher();
        if (teacher == null) {
            return "redirect:/login";
        }

        // TODO: Implement after fixing model relationships
        redirectAttributes.addFlashAttribute("message", "Tính năng đang phát triển!");
        return "redirect:/teacher/classes/" + classId;
    }
    */
}

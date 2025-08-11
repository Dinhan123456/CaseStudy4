
package com.codegym.module4casestudy.controller;

import com.codegym.module4casestudy.model.User;
import com.codegym.module4casestudy.model.Class;
import com.codegym.module4casestudy.model.Subject;
import com.codegym.module4casestudy.service.IUserService;
import com.codegym.module4casestudy.service.IClassService;
import com.codegym.module4casestudy.service.ISubjectService;
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

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private IUserService userService;

    @Autowired
    private IClassService classService;

    @Autowired
    private ISubjectService subjectService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Helper method để lấy thông tin sinh viên hiện tại
    private User getCurrentStudent() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            System.out.println("DEBUG: Authentication is null");
            return null;
        }

        String username = auth.getName();
        System.out.println("DEBUG: Current username: " + username);

        // Kiểm tra nếu username là "anonymousUser" hoặc null
        if (username == null || username.equals("anonymousUser")) {
            System.out.println("DEBUG: Username is anonymousUser or null");
            return null;
        }

        User student = userService.findByUsername(username).orElse(null);
        if (student == null) {
            System.out.println("DEBUG: Student not found for username: " + username);
        } else {
            System.out.println("DEBUG: Student found: " + student.getFullName() + " (ID: " + student.getId() + ")");
        }

        return student;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        var user = userService.findByUsername(principal.getName());
//        Long studentId = user.getId();
        Long studentId = user.get().getId();

        List<Class> recentClasses = classService.findClassesByStudentId(studentId);
        model.addAttribute("recentClasses",
                recentClasses != null ? recentClasses : Collections.emptyList());

        return "student/student-panel";
    }



    @GetMapping("/dashboard-simple")
    public String showStudentDashboardSimple(Model model) {
        System.out.println("DEBUG: Entering showStudentDashboardSimple");

        try {
            User student = getCurrentStudent();
            System.out.println("DEBUG: Student object: " + (student != null ? student.getFullName() : "null"));

            if (student == null) {
                System.out.println("DEBUG: Student is null, creating dummy student");
                model.addAttribute("error", "Không tìm thấy thông tin sinh viên! Vui lòng đăng nhập lại.");
                student = new User();
                student.setUsername("Unknown");
                student.setFullName("Không xác định");
                student.setEmail("");
                student.setPhone("");
            }

            List<Class> studentClasses = classService.findAll();
            System.out.println("DEBUG: Found " + studentClasses.size() + " classes");

            model.addAttribute("student", student);
            model.addAttribute("totalClasses", studentClasses.size());
            model.addAttribute("recentClasses", studentClasses.size() > 3 ? studentClasses.subList(0, 3) : studentClasses);

            System.out.println("DEBUG: Returning student-panel-simple template");
            return "student/student-panel-simple";

        } catch (Exception e) {
            System.out.println("DEBUG: Exception in showStudentDashboardSimple: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());

            User dummyStudent = new User();
            dummyStudent.setUsername("Unknown");
            dummyStudent.setFullName("Không xác định");
            dummyStudent.setEmail("");
            dummyStudent.setPhone("");

            model.addAttribute("student", dummyStudent);
            model.addAttribute("totalClasses", 0);
            model.addAttribute("recentClasses", new ArrayList<>());

            return "student/student-panel-simple";
        }
    }

    @GetMapping("/dashboard-fixed")
    public String showStudentDashboardFixed(Model model) {
        System.out.println("DEBUG: Entering showStudentDashboardFixed");

        try {
            User student = getCurrentStudent();
            System.out.println("DEBUG: Student object: " + (student != null ? student.getFullName() : "null"));

            if (student == null) {
                System.out.println("DEBUG: Student is null, creating dummy student");
                model.addAttribute("error", "Không tìm thấy thông tin sinh viên! Vui lòng đăng nhập lại.");
                student = new User();
                student.setUsername("Unknown");
                student.setFullName("Không xác định");
                student.setEmail("");
                student.setPhone("");
            }

            List<Class> studentClasses = classService.findAll();
            System.out.println("DEBUG: Found " + studentClasses.size() + " classes");

            model.addAttribute("student", student);
            model.addAttribute("totalClasses", studentClasses.size());
            model.addAttribute("recentClasses", studentClasses.size() > 3 ? studentClasses.subList(0, 3) : studentClasses);

            System.out.println("DEBUG: Returning student-panel-fixed template");
            return "student/student-panel-fixed";

        } catch (Exception e) {
            System.out.println("DEBUG: Exception in showStudentDashboardFixed: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());

            User dummyStudent = new User();
            dummyStudent.setUsername("Unknown");
            dummyStudent.setFullName("Không xác định");
            dummyStudent.setEmail("");
            dummyStudent.setPhone("");

            model.addAttribute("student", dummyStudent);
            model.addAttribute("totalClasses", 0);
            model.addAttribute("recentClasses", new ArrayList<>());

            return "student/student-panel-fixed";
        }
    }

    @GetMapping("/debug")
    public String debugStudent(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User student = getCurrentStudent();

        model.addAttribute("auth", auth);
        model.addAttribute("student", student);
        model.addAttribute("authName", auth != null ? auth.getName() : "null");
        model.addAttribute("authDetails", auth != null ? auth.getDetails() : "null");
        model.addAttribute("authAuthorities", auth != null ? auth.getAuthorities() : "null");

        return "student/student-debug";
    }

    @GetMapping("/test")
    public String testStudent(Model model) {
        System.out.println("DEBUG: Test endpoint called");
        model.addAttribute("message", "Test endpoint working!");
        return "student/student-test";
    }

    @GetMapping("/test-simple")
    public String testSimple() {
        System.out.println("DEBUG: Test simple endpoint called");
        return "student/student-test-simple";
    }

    @GetMapping("/session")
    public String checkSession(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User student = getCurrentStudent();

        System.out.println("DEBUG: Session check - Auth: " + (auth != null ? auth.getName() : "null"));
        System.out.println("DEBUG: Session check - Student: " + (student != null ? student.getFullName() : "null"));

        model.addAttribute("auth", auth);
        model.addAttribute("student", student);
        model.addAttribute("authName", auth != null ? auth.getName() : "null");
        model.addAttribute("authDetails", auth != null ? auth.getDetails() : "null");
        model.addAttribute("authAuthorities", auth != null ? auth.getAuthorities() : "null");

        return "student/student-session";
    }

    // Hiển thị thông tin cá nhân của sinh viên
    @GetMapping("/profile")
    public String showStudentProfile(Model model) {
        User student = getCurrentStudent();
        if (student == null) {
            model.addAttribute("error", "Không tìm thấy thông tin sinh viên! Vui lòng đăng nhập lại.");
            // Thêm một student mẫu để template không bị lỗi
            student = new User();
            student.setUsername("Unknown");
            student.setFullName("Không xác định");
            student.setEmail("");
            student.setPhone("");
        }

        model.addAttribute("student", student);
        return "student/student-profile";
    }

    @PostMapping("/update-profile")
    public String updateStudentProfile(
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String fullName,
            @RequestParam(required = false) String passwordOld,
            @RequestParam(required = false) String password,
            RedirectAttributes redirectAttributes) {

        try {
            User student = getCurrentStudent();
            if (student == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin sinh viên!");
                return "redirect:/student/profile";
            }

            // Cập nhật thông tin cơ bản
            student.setEmail(email);
            student.setPhone(phone);
            student.setFullName(fullName);

            // Xử lý đổi mật khẩu
            if (password != null && !password.trim().isEmpty()) {
                if (passwordOld == null || passwordOld.trim().isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Vui lòng nhập mật khẩu cũ!");
                    return "redirect:/student/profile";
                }

                // Kiểm tra mật khẩu cũ
                if (!passwordEncoder.matches(passwordOld, student.getPassword())) {
                    redirectAttributes.addFlashAttribute("error", "Mật khẩu cũ không đúng!");
                    return "redirect:/student/profile";
                }

                // Mã hóa và cập nhật mật khẩu mới
                student.setPassword(passwordEncoder.encode(password));
            }

            // Lưu thông tin
            userService.save(student);
            redirectAttributes.addFlashAttribute("message", "Cập nhật thông tin thành công!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi cập nhật thông tin!");
        }

        return "redirect:/student/profile";
    }

    @GetMapping("/classes")
    public String showStudentClasses(Model model) {
        System.out.println("DEBUG: Entering showStudentClasses");
        User student = getCurrentStudent();
        System.out.println("DEBUG: Student in classes: " + (student != null ? student.getFullName() : "null"));

        if (student == null) {
            System.out.println("DEBUG: Student is null in classes, creating dummy student");
            model.addAttribute("error", "Không tìm thấy thông tin sinh viên! Vui lòng đăng nhập lại.");
            // Thêm một student mẫu để template không bị lỗi
            student = new User();
            student.setUsername("Unknown");
            student.setFullName("Không xác định");
            student.setEmail("");
            student.setPhone("");
        }

        // Lấy các lớp mà student đã đăng ký
        List<Class> studentClasses = classService.findClassesByStudentId(student.getId());
        // Lấy các lớp mà student chưa đăng ký
        List<Class> allClasses = classService.findAll();
        List<Class> availableClasses = allClasses.stream()
                .filter(c -> !studentClasses.contains(c))
                .collect(Collectors.toList());

        model.addAttribute("student", student);
        model.addAttribute("studentClasses", studentClasses);
        model.addAttribute("availableClasses", availableClasses);

        System.out.println("DEBUG: Returning student-classes template");
        return "student/student-classes";
    }

    @GetMapping("/classes/{classId}")
    public String showClassDetails(@PathVariable Long classId, Model model) {
        User student = getCurrentStudent();
        if (student == null) {
            model.addAttribute("error", "Không tìm thấy thông tin sinh viên! Vui lòng đăng nhập lại.");
            return "redirect:/login";
        }

        Class studentClass = classService.findById(classId).orElse(null);
        if (studentClass == null) {
            model.addAttribute("error", "Không tìm thấy lớp học!");
            return "redirect:/student/classes";
        }

        // Kiểm tra xem student có trong lớp này không
        boolean isEnrolled = classService.findClassesByStudentId(student.getId())
                .stream()
                .anyMatch(c -> c.getId().equals(classId));

        model.addAttribute("student", student);
        model.addAttribute("class", studentClass);
        model.addAttribute("isEnrolled", isEnrolled);

        return "student/student-class-details";
    }

    @GetMapping("/grades")
    public String showStudentGrades(Model model) {
        System.out.println("DEBUG: Entering showStudentGrades");
        User student = getCurrentStudent();
        System.out.println("DEBUG: Student in grades: " + (student != null ? student.getFullName() : "null"));

        if (student == null) {
            System.out.println("DEBUG: Student is null in grades, creating dummy student");
            model.addAttribute("error", "Không tìm thấy thông tin sinh viên! Vui lòng đăng nhập lại.");
            // Thêm một student mẫu để template không bị lỗi
            student = new User();
            student.setUsername("Unknown");
            student.setFullName("Không xác định");
            student.setEmail("");
            student.setPhone("");
        }

        // Lấy các lớp mà student đã đăng ký
        List<Class> studentClasses = classService.findClassesByStudentId(student.getId());
        List<Subject> subjects = subjectService.findActiveSubjects();

        model.addAttribute("student", student);
        model.addAttribute("classes", studentClasses);
        model.addAttribute("subjects", subjects);

        System.out.println("DEBUG: Returning student-grades template");
        return "student/student-grades";
    }

    @GetMapping("/schedule")
    public String showStudentSchedule(Model model) {
        System.out.println("DEBUG: Entering showStudentSchedule");
        User student = getCurrentStudent();
        System.out.println("DEBUG: Student in schedule: " + (student != null ? student.getFullName() : "null"));

        if (student == null) {
            System.out.println("DEBUG: Student is null in schedule, creating dummy student");
            model.addAttribute("error", "Không tìm thấy thông tin sinh viên! Vui lòng đăng nhập lại.");
            // Thêm một student mẫu để template không bị lỗi
            student = new User();
            student.setUsername("Unknown");
            student.setFullName("Không xác định");
            student.setEmail("");
            student.setPhone("");
        }

        // Lấy các lớp mà student đã đăng ký
        List<Class> studentClasses = classService.findClassesByStudentId(student.getId());
        model.addAttribute("student", student);
        model.addAttribute("classes", studentClasses);

        System.out.println("DEBUG: Returning student-schedule template");
        return "student/student-schedule";
    }

    @PostMapping("/classes/{classId}/register")
    public String registerClass(
            @PathVariable Long classId,
            RedirectAttributes redirectAttributes) {

        User student = getCurrentStudent();
        if (student == null) {
            return "redirect:/login";
        }

        try {
            Class classToRegister = classService.findById(classId).orElse(null);
            if (classToRegister == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy lớp học!");
                return "redirect:/student/classes";
            }

            // Kiểm tra xem đã đăng ký chưa
            List<Class> enrolledClasses = classService.findClassesByStudentId(student.getId());
            if (enrolledClasses.stream().anyMatch(c -> c.getId().equals(classId))) {
                redirectAttributes.addFlashAttribute("error", "Bạn đã đăng ký lớp này rồi!");
                return "redirect:/student/classes";
            }

            // Đăng ký lớp học
            classService.addStudentToClass(classId, student.getId());
            redirectAttributes.addFlashAttribute("message", "Đăng ký lớp học thành công!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi đăng ký lớp!");
        }

        return "redirect:/student/classes";
    }

    @PostMapping("/classes/{classId}/unregister")
    public String unregisterClass(
            @PathVariable Long classId,
            RedirectAttributes redirectAttributes) {

        User student = getCurrentStudent();
        if (student == null) {
            return "redirect:/login";
        }

        try {
            Class classToUnregister = classService.findById(classId).orElse(null);
            if (classToUnregister == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy lớp học!");
                return "redirect:/student/classes";
            }

            // Kiểm tra xem có đăng ký chưa
            List<Class> enrolledClasses = classService.findClassesByStudentId(student.getId());
            if (enrolledClasses.stream().noneMatch(c -> c.getId().equals(classId))) {
                redirectAttributes.addFlashAttribute("error", "Bạn chưa đăng ký lớp này!");
                return "redirect:/student/classes";
            }

            // Hủy đăng ký lớp học
            classService.removeStudentFromClass(classId, student.getId());
            redirectAttributes.addFlashAttribute("message", "Hủy đăng ký lớp học thành công!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi hủy đăng ký lớp!");
        }

        return "redirect:/student/classes";
    }
}

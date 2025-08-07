package com.codegym.module4casestudy.controller;

import com.codegym.module4casestudy.model.Role;
import com.codegym.module4casestudy.model.User;
import com.codegym.module4casestudy.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    @Autowired
    private IUserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/admin/panel")
    public String adminPanel() {
        return "admin/admin-panel";
    }

    @GetMapping("/admin/info")
    public String adminInfo() {
        return "admin/admin-info";
    }

    @GetMapping("/admin/grades")
    public String adminGrades() {
        return "redirect:/grades";
    }

    @GetMapping("/admin/info-admin")
    public String adminInfoAdmin(Model model) {
        // Lấy thông tin admin hiện tại
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String username = auth.getName();
            User admin = userService.findByUsername(username).orElse(null);
            model.addAttribute("admin", admin);
        }
        return "admin/admin-info-admin";
    }

    @GetMapping("/admin/info-teacher")
    public String adminInfoTeacher(Model model) {
        // Lấy danh sách tất cả giảng viên
        List<User> teachers = userService.findByRole(Role.TEACHER);

        model.addAttribute("teachers", teachers);
        model.addAttribute("defaultPassword", "123456"); // Mật khẩu mặc định
        return "admin/admin-info-teacher";
    }

    @GetMapping("/admin/info-student")
    public String adminInfoStudent(Model model) {
        // Lấy danh sách tất cả sinh viên
        List<User> students = userService.findByRole(Role.STUDENT);

        model.addAttribute("students", students);
        model.addAttribute("defaultPassword", "123456"); // Mật khẩu mặc định
        return "admin/admin-info-student";
    }

    @GetMapping("/admin/register")
    public String adminRegister(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", new Role[]{Role.TEACHER, Role.STUDENT});
        return "admin/admin-register";
    }

    @PostMapping("/admin/register")
    public String adminRegisterPost(@RequestParam String username,
                                  @RequestParam String fullName,
                                  @RequestParam String email,
                                  @RequestParam String phone,
                                  @RequestParam Role role,
                                  RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra xem username đã tồn tại chưa
            if (userService.findByUsername(username).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Username đã tồn tại!");
                return "redirect:/admin/register";
            }

            // Tạo user mới
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setFullName(fullName);
            newUser.setEmail(email);
            newUser.setPhone(phone);
            newUser.setRole(role);
            newUser.setEnabled(true);

            // Set mật khẩu mặc định và mã hóa
            String defaultPassword = generateDefaultPassword(username);
            newUser.setPassword(passwordEncoder.encode(defaultPassword));

            userService.save(newUser);

            String roleText = (role == Role.TEACHER) ? "giảng viên" : "sinh viên";
            redirectAttributes.addFlashAttribute("message",
                "Đã tạo tài khoản " + roleText + " thành công! Username: " + username + ", Mật khẩu: " + defaultPassword);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi tạo tài khoản!");
        }
        return "redirect:/admin/register";
    }

    private String generateDefaultPassword(String username) {
        // Tạo mật khẩu mặc định: "123456" cho tất cả
        return "123456";
    }

    @PostMapping("/admin/reset-teacher-password")
    public String resetTeacherPassword(@RequestParam String username, RedirectAttributes redirectAttributes) {
        try {
            User teacher = userService.findByUsername(username).orElse(null);
            if (teacher != null && teacher.getRole() == Role.TEACHER) {
                String defaultPassword = generateDefaultPassword(username);
                teacher.setPassword(passwordEncoder.encode(defaultPassword));
                userService.save(teacher);
                redirectAttributes.addFlashAttribute("message",
                    "Đã reset mật khẩu cho " + teacher.getFullName() + " thành: " + defaultPassword);
            } else {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy giảng viên!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi reset mật khẩu!");
        }
        return "redirect:/admin/info-teacher";
    }

    @PostMapping("/admin/reset-student-password")
    public String resetStudentPassword(@RequestParam String username, RedirectAttributes redirectAttributes) {
        try {
            User student = userService.findByUsername(username).orElse(null);
            if (student != null && student.getRole() == Role.STUDENT) {
                String defaultPassword = generateDefaultPassword(username);
                student.setPassword(passwordEncoder.encode(defaultPassword));
                userService.save(student);
                redirectAttributes.addFlashAttribute("message",
                    "Đã reset mật khẩu cho " + student.getFullName() + " thành: " + defaultPassword);
            } else {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy sinh viên!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi reset mật khẩu!");
        }
        return "redirect:/admin/info-student";
    }

    @PostMapping("/admin/update-profile")
    public String updateAdminProfile(@RequestParam String email,
                                   @RequestParam String phone,
                                   @RequestParam(required = false) String passwordOld,
                                   @RequestParam(required = false) String password,
                                   RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                String username = auth.getName();
                User admin = userService.findByUsername(username).orElse(null);

                if (admin != null) {
                    // Cập nhật email và phone
                    admin.setEmail(email);
                    admin.setPhone(phone);

                    // Nếu có mật khẩu mới, cập nhật mật khẩu
                    if (password != null && !password.trim().isEmpty()) {
                        // Kiểm tra mật khẩu cũ nếu có
                        if (passwordOld != null && !passwordOld.trim().isEmpty()) {
                            if (!passwordEncoder.matches(passwordOld, admin.getPassword())) {
                                redirectAttributes.addFlashAttribute("error", "Mật khẩu cũ không đúng!");
                                return "redirect:/admin/info-admin";
                            }
                        }
                        // Mã hóa và cập nhật mật khẩu mới
                        admin.setPassword(passwordEncoder.encode(password));
                    }

                    userService.save(admin);
                    redirectAttributes.addFlashAttribute("message", "Cập nhật thông tin thành công!");
                }
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi cập nhật thông tin!");
        }
        return "redirect:/admin/info-admin";
    }

    @GetMapping("/teacher/panel")
    public String teacherPanel() {
        return "teacher/teacher-panel";
    }

    @GetMapping("/student/panel")
    public String studentPanel() {
        return "redirect:/student/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        // Lấy thông tin user hiện tại và redirect đến panel phù hợp
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String username = auth.getName();
            User user = userService.findByUsername(username).orElse(null);

            if (user != null) {
                switch (user.getRole()) {
                    case ADMIN:
                        return "redirect:/admin/panel";
                    case TEACHER:
                        return "redirect:/teacher/panel";
                    case STUDENT:
                        return "redirect:/student/panel";
                    default:
                        return "redirect:/home";
                }
            }
        }
        return "redirect:/login";
    }
}

package com.codegym.module4casestudy.controller;

import com.codegym.module4casestudy.model.Role;
import com.codegym.module4casestudy.model.User;
import com.codegym.module4casestudy.service.IUserService;
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

@Controller
@RequestMapping("/register")
public class RegisterController {
    @Autowired
    private IUserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public String showRegisterForm(Model model) {
        // Kiểm tra nếu user đã đăng nhập rồi thì redirect về dashboard
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return "redirect:/dashboard";
        }
        return "register/register";
    }

    @PostMapping
    public String processRegister(@RequestParam String fullname,
                                  @RequestParam String email,
                                  @RequestParam String phone,
                                  @RequestParam String password,
                                  @RequestParam String confirmPassword,
                                  @RequestParam String role,
                                  Model model) {
        
        // Validate các trường bắt buộc
        if (fullname == null || fullname.trim().isEmpty()) {
            model.addAttribute("error", "Họ và tên không được để trống");
            return "register/register";
        }
        if (email == null || email.trim().isEmpty()) {
            model.addAttribute("error", "Email không được để trống");
            return "register/register";
        }
        if (password == null || password.trim().isEmpty()) {
            model.addAttribute("error", "Mật khẩu không được để trống");
            return "register/register";
        }
        if (confirmPassword == null || !password.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp");
            return "register/register";
        }
        if (role == null || role.trim().isEmpty()) {
            model.addAttribute("error", "Vui lòng chọn vai trò");
            return "register/register";
        }

        // Kiểm tra email đã tồn tại chưa
        if (userService.findByUsername(email).isPresent()) {
            model.addAttribute("error", "Email này đã được sử dụng");
            return "register/register";
        }

        try {
            // Tạo user mới
            User newUser = new User();
            newUser.setFullName(fullname.trim());  // Sửa từ setFullname thành setFullName
            newUser.setUsername(email.trim());
            newUser.setEmail(email.trim());
            newUser.setPhone(phone != null ? phone.trim() : "");
            newUser.setPassword(passwordEncoder.encode(password));

            // Set role
            Role userRole = Role.valueOf(role.toUpperCase());
            newUser.setRole(userRole);

            // Lưu user
            userService.save(newUser);

            model.addAttribute("success", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "register/register";

        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra khi đăng ký: " + e.getMessage());
            return "register/register";
        }
    }
}

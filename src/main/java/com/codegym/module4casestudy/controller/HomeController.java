package com.codegym.module4casestudy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(@RequestParam(value = "logout", required = false) String logout, Model model) {
        // Kiểm tra xem user đã đăng nhập chưa
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            // Nếu đã đăng nhập, redirect đến dashboard phù hợp
            return "redirect:/dashboard";
        }

        // Nếu có logout parameter, thêm vào model để hiển thị thông báo
        if (logout != null) {
            model.addAttribute("logoutMessage", "Bạn đã đăng xuất thành công!");
        }

        // Nếu chưa đăng nhập, hiển thị trang home
        return "home/home";
    }

    @GetMapping("/home")
    public String showHome() {
        return "home/home";
    }
}

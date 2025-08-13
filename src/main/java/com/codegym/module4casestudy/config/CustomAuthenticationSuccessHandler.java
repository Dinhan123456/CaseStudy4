package com.codegym.module4casestudy.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {
        
        String username = authentication.getName();
        System.out.println("Authentication successful for user: " + username);
        
        // Sử dụng SecurityContext để lấy thông tin user thay vì gọi service
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .orElse("USER");
        
        System.out.println("User role: " + role);
        switch (role) {
            case "ADMIN":
                setDefaultTargetUrl("/admin/panel");
                System.out.println("Redirecting to admin panel");
                break;
            case "TEACHER":
                setDefaultTargetUrl("/teacher/panel");
                System.out.println("Redirecting to teacher panel");
                break;
            case "STUDENT":
                setDefaultTargetUrl("/student/panel");
                System.out.println("Redirecting to student panel");
                break;
            default:
                setDefaultTargetUrl("/dashboard");
                System.out.println("Redirecting to dashboard");
                break;
        }
        
        super.onAuthenticationSuccess(request, response, authentication);
    }
} 
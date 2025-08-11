package com.codegym.module4casestudy.config;

import com.codegym.module4casestudy.model.User;
import com.codegym.module4casestudy.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {
        
        String username = authentication.getName();
        System.out.println("Authentication successful for user: " + username);
        
        // Lazy load userService to avoid circular dependency
        IUserService userService = applicationContext.getBean(IUserService.class);
        User user = userService.findByUsername(username).orElse(null);
        
        if (user != null) {
            System.out.println("User found with role: " + user.getRole());
            switch (user.getRole()) {
                case ADMIN:
                    setDefaultTargetUrl("/admin/panel");
                    System.out.println("Redirecting to admin panel");
                    break;
                case TEACHER:
                    setDefaultTargetUrl("/teacher/panel");
                    System.out.println("Redirecting to teacher panel");
                    break;
                case STUDENT:
                    setDefaultTargetUrl("/student/panel");
                    System.out.println("Redirecting to student panel");
                    break;
                default:
                    setDefaultTargetUrl("/dashboard");
                    System.out.println("Redirecting to dashboard");
                    break;
            }
        } else {
            System.out.println("User not found, redirecting to dashboard");
            setDefaultTargetUrl("/dashboard");
        }
        
        super.onAuthenticationSuccess(request, response, authentication);
    }
} 
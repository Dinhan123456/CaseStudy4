package com.codegym.module4casestudy.controller;

import com.codegym.module4casestudy.model.User;
import com.codegym.module4casestudy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/users")
    public String testUsers() {
        try {
            List<User> users = userRepository.findAll();
            StringBuilder result = new StringBuilder("Users in database:\n");
            for (User user : users) {
                result.append("Username: ").append(user.getUsername())
                      .append(", Role: ").append(user.getRole())
                      .append(", Enabled: ").append(user.isEnabled())
                      .append(", Password: ").append(user.getPassword())
                      .append("\n");
            }
            return result.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/password")
    public String testPassword() {
        String rawPassword = "123456";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
        
        return "Raw password: " + rawPassword + "\n" +
               "Encoded password: " + encodedPassword + "\n" +
               "Matches: " + matches;
    }

    @GetMapping("/auth")
    public String testAuth(@RequestParam String username, @RequestParam String password) {
        try {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());
                
                return "Username: " + username + "\n" +
                       "User found: " + user.getUsername() + "\n" +
                       "Role: " + user.getRole() + "\n" +
                       "Enabled: " + user.isEnabled() + "\n" +
                       "Password matches: " + passwordMatches + "\n" +
                       "Stored password: " + user.getPassword();
            } else {
                return "User not found: " + username;
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
} 
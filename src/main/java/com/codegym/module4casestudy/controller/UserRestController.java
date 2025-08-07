package com.codegym.module4casestudy.controller;

import com.codegym.module4casestudy.model.User;
import com.codegym.module4casestudy.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserRestController {

    @Autowired
    private IUserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        // Mã hóa password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(true);
        
        // Lưu user (cần implement method save trong UserService)
        // User savedUser = userService.save(user);
        // return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        // Note: In a real application, you should return DTOs instead of entities
        // and implement proper pagination
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        // Note: In a real application, you should return DTOs instead of entities
        return ResponseEntity.ok().build();
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getCurrentUserProfile() {
        // Note: In a real application, you should return DTOs instead of entities
        return ResponseEntity.ok().build();
    }

    @PutMapping("/profile")
    public ResponseEntity<User> updateCurrentUserProfile(@RequestBody User user) {
        // Note: In a real application, you should use DTOs and implement proper validation
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        // Note: In a real application, implement proper user deletion logic
        return ResponseEntity.noContent().build();
    }
}
package com.codegym.module4casestudy.service;

import com.codegym.module4casestudy.model.User;
import com.codegym.module4casestudy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("=== UserDetailsServiceImpl.loadUserByUsername ===");
        System.out.println("Trying to load user: " + username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    System.out.println("User not found: " + username);
                    return new UsernameNotFoundException("User not found: " + username);
                });

        System.out.println("Found user: " + user.getUsername() + " with role: " + user.getRole());
        System.out.println("User password in DB: " + user.getPassword());
        System.out.println("User enabled: " + user.isEnabled());

        // Test password matching
//        boolean passwordMatches = passwordEncoder.matches("123456", user.getPassword());
//        System.out.println("Password '123456' matches stored password: " + passwordMatches);

//        // Kiểm tra xem mật khẩu đã được mã hóa chưa
//        String password = user.getPassword();
//        if (!password.startsWith("$2a$") && !password.startsWith("$2b$") && !password.startsWith("$2y$")) {
//            // Mật khẩu chưa được mã hóa, mã hóa nó
//            System.out.println("Password is not encoded, encoding now...");
//            password = passwordEncoder.encode(password);
//            user.setPassword(password);
//            // Lưu lại mật khẩu đã mã hóa vào database
//            userRepository.save(user);
//            System.out.println("Password encoded and saved: " + password);
//        }


        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                true,
                true,
                true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}

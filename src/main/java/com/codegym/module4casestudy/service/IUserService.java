package com.codegym.module4casestudy.service;

import com.codegym.module4casestudy.model.Role;
import com.codegym.module4casestudy.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

public interface IUserService {
    Optional<User> findByUsername(String username);
    void save(User user);
    List<User> findByRole(Role role); //Nhi thêm method ở findByRole trong UserService và UserRepository để lấy danh sách giảng viên

    // Thêm các method cần thiết cho ClassController
    List<User> findAllStudents();
    List<User> findAllTeachers();
    List<User> findAll();
}

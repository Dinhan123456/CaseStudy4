package com.codegym.module4casestudy.service;

import com.codegym.module4casestudy.model.Role;
import com.codegym.module4casestudy.model.User;
import com.codegym.module4casestudy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public void save(User user) {
        userRepository.save(user);
    }

    // Nhi tạo method findByRole để lấy danh sách giảng viên
    @Override
    public List<User> findByRole(Role role) {
        return userRepository.findByRole(role);
    }

    // Implementation các method mới cho ClassController
    @Override
    public List<User> findAllStudents() {
        return userRepository.findByRole(Role.STUDENT);
    }

    @Override
    public List<User> findAllTeachers() {
        return userRepository.findByRole(Role.TEACHER);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }
}

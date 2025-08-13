package com.codegym.module4casestudy.repository;

import com.codegym.module4casestudy.model.Role;
import com.codegym.module4casestudy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    // Nhi tạo method findByRole để lấy danh sách giảng viên
    List<User> findByRole(Role role);
}

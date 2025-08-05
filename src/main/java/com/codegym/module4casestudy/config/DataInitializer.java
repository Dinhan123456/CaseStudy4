package com.codegym.module4casestudy.config;

import com.codegym.module4casestudy.model.Class;
import com.codegym.module4casestudy.model.Role;
import com.codegym.module4casestudy.model.User;
import com.codegym.module4casestudy.repository.ClassRepository;
import com.codegym.module4casestudy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // Chỉ tạo dữ liệu nếu chưa có user nào
        if (userRepository.count() == 0) {
            createInitialUsers();
        }

        // Chỉ tạo dữ liệu nếu chưa có class nào
        if (classRepository.count() == 0) {
            createInitialClasses();
        }
    }

    private void createInitialUsers() {

        // Tạo admin
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("123456"));
        admin.setFullName("Administrator");
        admin.setEmail("admin@codegym.vn");
        admin.setPhone("0123456789");
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);
        userRepository.save(admin);

        // Tạo teacher1
        User teacher1 = new User();
        teacher1.setUsername("teacher1");
        teacher1.setPassword(passwordEncoder.encode("123456"));
        teacher1.setFullName("Nguyễn Văn Giáo Viên");
        teacher1.setEmail("teacher1@codegym.vn");
        teacher1.setPhone("0123456788");
        teacher1.setRole(Role.TEACHER);
        teacher1.setEnabled(true);
        userRepository.save(teacher1);

        // Tạo student1
        User student1 = new User();
        student1.setUsername("student1");
        student1.setPassword(passwordEncoder.encode("123456"));
        student1.setFullName("Lê Văn Sinh Viên");
        student1.setEmail("student1@codegym.vn");
        student1.setPhone("0123456787");
        student1.setRole(Role.STUDENT);
        student1.setEnabled(true);
        userRepository.save(student1);

        System.out.println("Đã tạo dữ liệu user ban đầu!");
    }

    private void createInitialClasses() {
        // Tạo các lớp học mẫu
        Class class1 = new Class("C0323G1", "Lớp Java Web Full Stack");
        classRepository.save(class1);

        Class class2 = new Class("C0323G2", "Lớp Java Backend");
        classRepository.save(class2);

        Class class3 = new Class("C0323G3", "Lớp Frontend React");
        classRepository.save(class3);

        System.out.println("Đã tạo dữ liệu lớp học ban đầu!");
    }
}
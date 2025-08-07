package com.codegym.module4casestudy.controller;

import com.codegym.module4casestudy.service.IClassService;
import com.codegym.module4casestudy.service.ISubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GradesController {

    @Autowired
    private IClassService classService;

    @Autowired
    private ISubjectService subjectService;

    @GetMapping("/grades")
    public String viewAdminGrades(Model model) {
        // Truyền dữ liệu classes và subjects cho template
        model.addAttribute("classes", classService.findAll());
        model.addAttribute("subjects", subjectService.findActiveSubjects());

        return "admin/admin-grades"; // Tên file HTML trong templates/admin
    }
}

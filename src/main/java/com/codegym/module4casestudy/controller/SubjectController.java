package com.codegym.module4casestudy.controller;

//Nhi thêm lớp này để quản lí môn học:
// GET /subjects - Hiển thị danh sách môn học
//GET /subjects/search - Tìm kiếm môn học
//GET /subjects/new - Form thêm môn học mới
//POST /subjects/new - Xử lý thêm môn học
//GET /subjects/edit/{id} - Form chỉnh sửa
//POST /subjects/edit/{id} - Xử lý cập nhật
//GET /subjects/delete/{id} - Xóa môn học (soft delete)
import com.codegym.module4casestudy.model.Subject;
import com.codegym.module4casestudy.service.ISubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/subjects")
public class SubjectController {

    @Autowired
    private ISubjectService subjectService;

    @GetMapping
    public String listSubjects(Model model) {
        List<Subject> subjects = subjectService.findActiveSubjects();
        model.addAttribute("subjects", subjects);
        return "admin/admin-subject";
    }

    @GetMapping("/search")
    public String searchSubjects(@RequestParam(required = false) String keyword, Model model) {
        List<Subject> subjects = subjectService.searchByKeyword(keyword);
        model.addAttribute("subjects", subjects);
        model.addAttribute("keyword", keyword);
        return "admin/admin-subject";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("subject", new Subject());
        return "admin/admin-subject-form";
    }

    @PostMapping("/new")
    public String createSubject(@ModelAttribute Subject subject, RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra tên môn học đã tồn tại chưa
            if (subjectService.existsByName(subject.getName())) {
                redirectAttributes.addFlashAttribute("error", "Tên môn học đã tồn tại!");
                return "redirect:/subjects/new";
            }

            subject.setActive(true);
            subjectService.save(subject);
            redirectAttributes.addFlashAttribute("message", "Thêm môn học thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi thêm môn học!");
        }
        return "redirect:/subjects";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Subject subject = subjectService.findById(id).orElse(null);
            if (subject == null || !subject.getActive()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy môn học!");
                return "redirect:/subjects";
            }
            model.addAttribute("subject", subject);
            return "admin/admin-subject-form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra!");
            return "redirect:/subjects";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateSubject(@PathVariable Long id, @ModelAttribute Subject subject, RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra tên môn học đã tồn tại chưa (trừ chính nó)
            if (subjectService.existsByNameAndIdNot(subject.getName(), id)) {
                redirectAttributes.addFlashAttribute("error", "Tên môn học đã tồn tại!");
                return "redirect:/subjects/edit/" + id;
            }

            Subject existingSubject = subjectService.findById(id).orElse(null);
            if (existingSubject == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy môn học!");
                return "redirect:/subjects";
            }

            existingSubject.setName(subject.getName());
            existingSubject.setCredits(subject.getCredits());
            existingSubject.setDescription(subject.getDescription());

            subjectService.save(existingSubject);
            redirectAttributes.addFlashAttribute("message", "Cập nhật môn học thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi cập nhật môn học!");
        }
        return "redirect:/subjects";
    }

    @GetMapping("/delete/{id}")
    public String deleteSubject(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Subject subject = subjectService.findById(id).orElse(null);
            if (subject == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy môn học!");
                return "redirect:/subjects";
            }

            subjectService.deleteById(id);
            redirectAttributes.addFlashAttribute("message", "Xóa môn học thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi xóa môn học!");
        }
        return "redirect:/subjects";
    }
}

package com.codegym.module4casestudy.controller;

// xu li ác request của class

import com.codegym.module4casestudy.model.Class;
import com.codegym.module4casestudy.model.ClassSubject;
import com.codegym.module4casestudy.model.Role;
import com.codegym.module4casestudy.service.IClassService;
import com.codegym.module4casestudy.service.IUserService;
import com.codegym.module4casestudy.service.ISubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/classes")
public class ClassController {

    @Autowired
    private IClassService classService;  // Sử dụng interface thay vì concrete class để tránh proxy issue

    @Autowired
    private IUserService userService;

    @Autowired
    private ISubjectService subjectService;

    @GetMapping
    public String listClasses(Model model) {
        List<Class> classes = classService.findAllWithStudentsAndTeachers();
        model.addAttribute("classes", classes);
        return "admin/admin-class";
    }

    @GetMapping("/search")
    public String searchClasses(@RequestParam(required = false) String keyword, Model model) {
        List<Class> classes = classService.searchByKeyword(keyword);
        model.addAttribute("classes", classes);
        model.addAttribute("keyword", keyword);
        return "admin/admin-class";
    }

    @GetMapping("/{id}/details")
    public String classDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Class classEntity = classService.findByIdWithStudentsAndTeachers(id);
            if (classEntity == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy lớp học!");
                return "redirect:/classes";
            }

            // Lấy thông tin môn học và giảng viên dạy
            List<ClassSubject> classSubjects = classService.getClassSubjects(id);

            model.addAttribute("classEntity", classEntity);
            model.addAttribute("classSubjects", classSubjects);
            return "admin/admin-class-details";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra!");
            return "redirect:/classes";
        }
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("classEntity", new Class());
        // Thêm dữ liệu cần thiết cho form
        model.addAttribute("allStudents", userService.findByRole(Role.STUDENT));
        model.addAttribute("allTeachers", userService.findByRole(Role.TEACHER));
        model.addAttribute("allSubjects", subjectService.findAll());
        model.addAttribute("classSubjects", Collections.emptyList()); // để view không bị null
        return "admin/admin-class-form";
    }

    @PostMapping("/new")
    public String createClass(@Valid @ModelAttribute Class classEntity, 
                            BindingResult bindingResult, 
                            RedirectAttributes redirectAttributes,
                            Model model) {
        try {
            // Validation
            if (bindingResult.hasErrors()) {
                // Thêm lại dữ liệu cần thiết cho form
                model.addAttribute("allStudents", userService.findByRole(Role.STUDENT));
                model.addAttribute("allTeachers", userService.findByRole(Role.TEACHER));
                model.addAttribute("allSubjects", subjectService.findAll());
                return "admin/admin-class-form";
            }

            // Kiểm tra tên lớp học đã tồn tại chưa
            if (classService.existsByName(classEntity.getName())) {
                redirectAttributes.addFlashAttribute("error", "Tên lớp học đã tồn tại!");
                model.addAttribute("allStudents", userService.findByRole(Role.STUDENT));
                model.addAttribute("allTeachers", userService.findByRole(Role.TEACHER));
                model.addAttribute("allSubjects", subjectService.findAll());
                return "admin/admin-class-form";
            }

            // Đảm bảo trạng thái active được set
//            if (classEntity.getActive() == null) {
//                classEntity.setActive(true);
//            }


            classService.save(classEntity);
            redirectAttributes.addFlashAttribute("message", "Thêm lớp học thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi thêm lớp học: " + e.getMessage());
            model.addAttribute("allStudents", userService.findByRole(Role.STUDENT));
            model.addAttribute("allTeachers", userService.findByRole(Role.TEACHER));
            model.addAttribute("allSubjects", subjectService.findAll());
            return "admin/admin-class-form";
        }
        return "redirect:/classes";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            java.util.Optional<Class> classOpt = classService.findById(id);
            if (!classOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy lớp học!");
                return "redirect:/classes";
            }
            Class classEntity = classOpt.get();
            // Sử dụng findById thay vì findByIdWithStudentsAndTeachers để tránh lỗi relationship
            model.addAttribute("classEntity", classEntity);
            model.addAttribute("allStudents", userService.findByRole(com.codegym.module4casestudy.model.Role.STUDENT));
            model.addAttribute("allTeachers", userService.findByRole(com.codegym.module4casestudy.model.Role.TEACHER));
            model.addAttribute("allSubjects", subjectService.findAll());
            // Lấy class subjects riêng để tránh lỗi
            try {
                model.addAttribute("classSubjects", classService.getClassSubjects(id));
            } catch (Exception e) {
                model.addAttribute("classSubjects", new java.util.ArrayList<>());
                System.out.println("Warning: Could not load class subjects: " + e.getMessage());
            }
            return "admin/admin-class-form";
        } catch (Exception e) {
            e.printStackTrace(); // Log full stack trace for debugging
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi tải thông tin lớp học: " + e.getMessage());
            return "redirect:/classes";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateClass(@PathVariable Long id, 
                            @Valid @ModelAttribute Class classEntity, 
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        try {
            // Validation
            if (bindingResult.hasErrors()) {
                // Thêm lại dữ liệu cần thiết cho form
                model.addAttribute("allStudents", userService.findByRole(Role.STUDENT));
                model.addAttribute("allTeachers", userService.findByRole(Role.TEACHER));
                model.addAttribute("allSubjects", subjectService.findAll());
                model.addAttribute("classSubjects", classService.getClassSubjects(id));
                return "admin/admin-class-form";
            }

            // Kiểm tra tên lớp học đã tồn tại chưa (trừ chính nó)
            if (classService.existsByNameAndIdNot(classEntity.getName(), id)) {
                redirectAttributes.addFlashAttribute("error", "Tên lớp học đã tồn tại!");
                model.addAttribute("allStudents", userService.findByRole(Role.STUDENT));
                model.addAttribute("allTeachers", userService.findByRole(Role.TEACHER));
                model.addAttribute("allSubjects", subjectService.findAll());
                model.addAttribute("classSubjects", classService.getClassSubjects(id));
                return "admin/admin-class-form";
            }

            Class existingClass = classService.findById(id).orElse(null);
            if (existingClass == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy lớp học!");
                return "redirect:/classes";
            }

            existingClass.setName(classEntity.getName());
            existingClass.setDescription(classEntity.getDescription());
            existingClass.setActive(classEntity.getActive());

            classService.save(existingClass);
            redirectAttributes.addFlashAttribute("message", "Cập nhật lớp học thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi cập nhật lớp học: " + e.getMessage());
            model.addAttribute("allStudents", userService.findByRole(Role.STUDENT));
            model.addAttribute("allTeachers", userService.findByRole(Role.TEACHER));
            model.addAttribute("allSubjects", subjectService.findAll());
            model.addAttribute("classSubjects", classService.getClassSubjects(id));
            return "admin/admin-class-form";
        }
        return "redirect:/classes";
    }

    @GetMapping("/delete/{id}")
    public String deleteClass(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Class classEntity = classService.findById(id).orElse(null);
            if (classEntity == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy lớp học!");
                return "redirect:/classes";
            }

            classService.deleteById(id);
            redirectAttributes.addFlashAttribute("message", "Xóa lớp học thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi xóa lớp học!");
        }
        return "redirect:/classes";
    }

    // API endpoints cho quản lý sinh viên trong lớp
    @PostMapping("/{classId}/students/{studentId}/add")
    @ResponseBody
    public String addStudentToClass(@PathVariable Long classId, @PathVariable Long studentId) {
        try {
            classService.addStudentToClass(classId, studentId);
            return "success";
        } catch (Exception e) {
            return "error";
        }
    }

    // Xử lý submit form thêm nhiều sinh viên vào lớp
    @PostMapping("/{id}/students")
    public String addStudentsForm(@PathVariable Long id,
                                  @RequestParam("studentIds") List<Long> studentIds,
                                  RedirectAttributes ra) {
        classService.addStudentsToClass(id, studentIds);
        ra.addFlashAttribute("msgSuccess","Thêm sinh viên vào lớp thành công!");
        return "redirect:/classes/edit/" + id;
    }

    @PostMapping("/{classId}/students/{studentId}/remove")
    @ResponseBody
    public String removeStudentFromClass(@PathVariable Long classId, @PathVariable Long studentId) {
        try {
            classService.removeStudentFromClass(classId, studentId);
            return "success";
        } catch (Exception e) {
            return "error";
        }
    }

    // API endpoints cho quản lý giảng viên trong lớp
    @PostMapping("/{classId}/teachers/{teacherId}/add")
    @ResponseBody
    public String addTeacherToClass(@PathVariable Long classId, @PathVariable Long teacherId) {
        try {
            classService.addTeacherToClass(classId, teacherId);
            return "success";
        } catch (Exception e) {
            return "error";
        }
    }

    @PostMapping("/{classId}/teachers/{teacherId}/remove")
    @ResponseBody
    public String removeTeacherFromClass(@PathVariable Long classId, @PathVariable Long teacherId) {
        try {
            classService.removeTeacherFromClass(classId, teacherId);
            return "success";
        } catch (Exception e) {
            return "error";
        }
    }

    // API endpoints cho quản lý môn học và giảng viên dạy
    @PostMapping("/{classId}/subjects/{subjectId}/teachers/{teacherId}/assign")
    @ResponseBody
    public String assignTeacherToSubject(@PathVariable Long classId, @PathVariable Long subjectId, @PathVariable Long teacherId) {
        try {
            classService.assignTeacherToSubject(classId, subjectId, teacherId);
            return "success";
        } catch (Exception e) {
            return "error";
        }
    }

    @PostMapping("/{classId}/subjects/{subjectId}/teachers/{teacherId}/remove")
    @ResponseBody
    public String removeTeacherFromSubject(@PathVariable Long classId, @PathVariable Long subjectId, @PathVariable Long teacherId) {
        try {
            classService.removeTeacherFromSubject(classId, subjectId, teacherId);
            return "success";
        } catch (Exception e) {
            return "error";
        }
    }
}

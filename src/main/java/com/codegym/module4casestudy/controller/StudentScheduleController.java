package com.codegym.module4casestudy.controller;

import com.codegym.module4casestudy.model.*;
import com.codegym.module4casestudy.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/student/schedules")
public class StudentScheduleController {

    @Autowired
    private IScheduleService scheduleService;
    
    @Autowired
    private IUserService userService;
    
    @Autowired
    private IClassService classService;
    
    @Autowired
    private ISubjectService subjectService;

    // Lấy thông tin sinh viên hiện tại
    private User getCurrentStudent() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            Optional<User> userOpt = userService.findByUsername(auth.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getRole() == Role.STUDENT) {
                    return user;
                }
            }
        }
        return null;
    }

    // Trang chính xem lịch học của sinh viên
    @GetMapping
    public String studentSchedulePage(Model model,
                                     @RequestParam(required = false) String week) {
        User student = getCurrentStudent();
        if (student == null) {
            return "redirect:/login";
        }

        // Xác định tuần hiện tại hoặc tuần được chọn
        LocalDate currentDate = LocalDate.now();
        LocalDate startOfWeek = currentDate.minusDays(currentDate.getDayOfWeek().getValue() - 1);
        
        if (week != null && !week.isEmpty()) {
            try {
                startOfWeek = LocalDate.parse(week, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (Exception e) {
                // Sử dụng tuần hiện tại nếu không parse được
            }
        }
        
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        // Lấy lịch học của sinh viên trong tuần
        List<Schedule> weeklySchedules = scheduleService.findStudentScheduleByWeek(
            student.getId(), startOfWeek, endOfWeek);
        
        // Lấy tất cả lịch học của sinh viên
        List<Schedule> allSchedules = scheduleService.findByStudentId(student.getId());

        model.addAttribute("student", student);
        model.addAttribute("weeklySchedules", weeklySchedules);
        model.addAttribute("allSchedules", allSchedules);
        model.addAttribute("currentWeek", startOfWeek);
        model.addAttribute("startOfWeek", startOfWeek);
        model.addAttribute("endOfWeek", endOfWeek);
        
        // Tuần trước và tuần sau
        model.addAttribute("previousWeek", startOfWeek.minusWeeks(1));
        model.addAttribute("nextWeek", startOfWeek.plusWeeks(1));

        return "student/student-schedule";
    }

    // Xem chi tiết lịch học
    @GetMapping("/{id}")
    public String viewScheduleDetail(@PathVariable Long id, Model model) {
        User student = getCurrentStudent();
        if (student == null) {
            return "redirect:/login";
        }

        Schedule schedule = scheduleService.findById(id);
        if (schedule == null) {
            return "redirect:/student/schedules";
        }

        // Kiểm tra xem sinh viên có trong lớp học này không
        List<Schedule> studentSchedules = scheduleService.findByStudentId(student.getId());
        boolean hasAccess = studentSchedules.stream()
                .anyMatch(s -> s.getId().equals(schedule.getId()));
        
        if (!hasAccess) {
            return "redirect:/student/schedules";
        }

        model.addAttribute("schedule", schedule);
        model.addAttribute("student", student);

        return "student/student-schedule-detail";
    }

    // Trang đăng ký môn học
    @GetMapping("/registration")
    public String courseRegistrationPage(Model model) {
        User student = getCurrentStudent();
        if (student == null) {
            return "redirect:/login";
        }

        // Lấy các môn học có thể đăng ký
        List<Subject> availableSubjects = subjectService.findAll();
        
        // Lấy các lớp học có thể đăng ký
        List<com.codegym.module4casestudy.model.Class> availableClasses = classService.findAll();
        
        // Lấy các lịch học hiện có để sinh viên có thể xem
        List<Schedule> availableSchedules = scheduleService.findAvailableSchedulesForRegistration();

        model.addAttribute("student", student);
        model.addAttribute("availableSubjects", availableSubjects);
        model.addAttribute("availableClasses", availableClasses);
        model.addAttribute("availableSchedules", availableSchedules);

        return "student/student-course-registration";
    }

    // Đăng ký môn học
    @PostMapping("/register")
    public String registerForCourse(@RequestParam Long scheduleId,
                                   RedirectAttributes redirectAttributes) {
        User student = getCurrentStudent();
        if (student == null) {
            return "redirect:/login";
        }

        try {
            Schedule schedule = scheduleService.findById(scheduleId);
            if (schedule == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy lịch học!");
                return "redirect:/student/schedules/registration";
            }

            // Kiểm tra xem sinh viên đã đăng ký lớp này chưa
            List<Schedule> studentSchedules = scheduleService.findByStudentId(student.getId());
            boolean alreadyRegistered = studentSchedules.stream()
                    .anyMatch(s -> s.getClassEntity().getId().equals(schedule.getClassEntity().getId()) 
                              && s.getSubject().getId().equals(schedule.getSubject().getId()));

            if (alreadyRegistered) {
                redirectAttributes.addFlashAttribute("error", "Bạn đã đăng ký môn học này rồi!");
                return "redirect:/student/schedules/registration";
            }

            // Kiểm tra xung đột lịch học
            boolean hasConflict = scheduleService.hasStudentScheduleConflict(
                student.getId(), schedule.getDayOfWeek() != null ? schedule.getDayOfWeek().ordinal() : null, schedule.getTimeSlot().getId(),
                schedule.getStartDate(), schedule.getEndDate());

            if (hasConflict) {
                redirectAttributes.addFlashAttribute("error", 
                    "Lịch học bị trùng với các môn đã đăng ký! Vui lòng chọn lịch khác.");
                return "redirect:/student/schedules/registration";
            }

            // Thực hiện đăng ký (thêm sinh viên vào lớp)
            boolean success = scheduleService.registerStudentForSchedule(student.getId(), scheduleId);
            
            if (success) {
                redirectAttributes.addFlashAttribute("message", 
                    "Đăng ký môn học thành công! Lịch học đã được thêm vào thời khóa biểu của bạn.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Đăng ký thất bại! Vui lòng thử lại.");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }

        return "redirect:/student/schedules/registration";
    }

    // Hủy đăng ký môn học
    @PostMapping("/unregister")
    public String unregisterFromCourse(@RequestParam Long scheduleId,
                                      RedirectAttributes redirectAttributes) {
        User student = getCurrentStudent();
        if (student == null) {
            return "redirect:/login";
        }

        try {
            boolean success = scheduleService.unregisterStudentFromSchedule(student.getId(), scheduleId);
            
            if (success) {
                redirectAttributes.addFlashAttribute("message", "Hủy đăng ký môn học thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Hủy đăng ký thất bại! Vui lòng thử lại.");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }

        return "redirect:/student/schedules";
    }

    // Lấy lịch theo tuần (AJAX)
    @GetMapping("/weekly")
    @ResponseBody
    public List<Schedule> getWeeklySchedule(@RequestParam Long studentId,
                                          @RequestParam String startDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = start.plusDays(6);
            return scheduleService.findStudentScheduleByWeek(studentId, start, end);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    // Lấy thông tin môn học có thể đăng ký (AJAX)
    @GetMapping("/available-courses")
    @ResponseBody
    public List<Schedule> getAvailableCourses(@RequestParam(required = false) Long subjectId) {
        try {
            if (subjectId != null) {
                return scheduleService.findAvailableSchedulesBySubject(subjectId);
            } else {
                return scheduleService.findAvailableSchedulesForRegistration();
            }
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    // Xuất lịch học sinh viên
    @GetMapping("/export")
    public String exportSchedule(Model model,
                                @RequestParam(required = false) String format,
                                @RequestParam(required = false) String period) {
        User student = getCurrentStudent();
        if (student == null) {
            return "redirect:/login";
        }

        // Mặc định xuất theo học kỳ hiện tại
        LocalDate startDate = LocalDate.now().withMonth(1).withDayOfMonth(1);
        LocalDate endDate = LocalDate.now().withMonth(12).withDayOfMonth(31);
        
        if ("week".equals(period)) {
            startDate = LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1);
            endDate = startDate.plusDays(6);
        } else if ("month".equals(period)) {
            startDate = LocalDate.now().withDayOfMonth(1);
            endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        }

        List<Schedule> schedules = scheduleService.findStudentScheduleByWeek(
            student.getId(), startDate, endDate);

        model.addAttribute("student", student);
        model.addAttribute("schedules", schedules);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("period", period != null ? period : "semester");

        return "student/student-schedule-export";
    }
}

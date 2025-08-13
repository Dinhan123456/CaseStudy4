package com.codegym.module4casestudy.controller;

import com.codegym.module4casestudy.model.*;
import com.codegym.module4casestudy.model.ScheduleStatus;
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
@RequestMapping("/teacher/schedules")
public class TeacherScheduleController {

    @Autowired
    private IScheduleService scheduleService;
    
    @Autowired
    private IUserService userService;
    // Lấy thông tin giảng viên hiện tại
    private User getCurrentTeacher() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            Optional<User> userOpt = userService.findByUsername(auth.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getRole() == Role.TEACHER) {
                    return user;
                }
            }
        }
        return null;
    }

    // Trang chính xem lịch giảng dạy
    @GetMapping
    public String teacherSchedulePage(Model model,
                                     @RequestParam(required = false) String week) {
        User teacher = getCurrentTeacher();
        if (teacher == null) {
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

        // Lấy lịch của giảng viên trong tuần
        List<Schedule> weeklySchedules = scheduleService.findTeacherScheduleByWeek(
            teacher.getId(), startOfWeek, endOfWeek);
        
        // Lấy tất cả lịch của giảng viên (để thống kê)
        List<Schedule> allSchedules = scheduleService.findByTeacherId(teacher.getId());
        
        // Lấy lịch chờ xác nhận
        List<Schedule> pendingSchedules = scheduleService.findPendingSchedulesByTeacher(teacher.getId());

        model.addAttribute("teacher", teacher);
        model.addAttribute("weeklySchedules", weeklySchedules);
        model.addAttribute("allSchedules", allSchedules);
        model.addAttribute("pendingSchedules", pendingSchedules);
        model.addAttribute("currentWeek", startOfWeek);
        model.addAttribute("startOfWeek", startOfWeek);
        model.addAttribute("endOfWeek", endOfWeek);
        
        // Tuần trước và tuần sau
        model.addAttribute("previousWeek", startOfWeek.minusWeeks(1));
        model.addAttribute("nextWeek", startOfWeek.plusWeeks(1));

        return "teacher/teacher-schedule";
    }

    // Xem chi tiết lịch
    @GetMapping("/{id}")
    public String viewScheduleDetail(@PathVariable Long id, Model model) {
        User teacher = getCurrentTeacher();
        if (teacher == null) {
            return "redirect:/login";
        }

        Schedule schedule = scheduleService.findById(id);
        if (schedule == null || !schedule.getTeacher().getId().equals(teacher.getId())) {
            return "redirect:/teacher/schedules";
        }

        model.addAttribute("schedule", schedule);
        model.addAttribute("teacher", teacher);

        return "teacher/teacher-schedule-detail";
    }

    // Xác nhận lịch giảng dạy
    @PostMapping("/{id}/confirm")
    public String confirmSchedule(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User teacher = getCurrentTeacher();
        if (teacher == null) {
            return "redirect:/login";
        }

        Schedule schedule = scheduleService.findById(id);
        if (schedule == null || !schedule.getTeacher().getId().equals(teacher.getId())) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy lịch học!");
            return "redirect:/teacher/schedules";
        }

        if (schedule.getStatus() == ScheduleStatus.PENDING) {
            schedule = scheduleService.confirmSchedule(id);
            if (schedule != null) {
                redirectAttributes.addFlashAttribute("message", "Đã xác nhận lịch giảng dạy!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Không thể xác nhận lịch!");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Lịch này đã được xử lý!");
        }

        return "redirect:/teacher/schedules";
    }

    // Yêu cầu thay đổi lịch
    @PostMapping("/{id}/request-change")
    public String requestScheduleChange(@PathVariable Long id,
                                       @RequestParam String reason,
                                       RedirectAttributes redirectAttributes) {
        User teacher = getCurrentTeacher();
        if (teacher == null) {
            return "redirect:/login";
        }

        Schedule schedule = scheduleService.findById(id);
        if (schedule == null || !schedule.getTeacher().getId().equals(teacher.getId())) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy lịch học!");
            return "redirect:/teacher/schedules";
        }

        // Thêm ghi chú yêu cầu thay đổi
        String currentNotes = schedule.getNotes() != null ? schedule.getNotes() : "";
        String changeRequest = "\n[Yêu cầu thay đổi từ " + teacher.getFullName() + " - " + 
                              LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
                              "]: " + reason;
        schedule.setNotes(currentNotes + changeRequest);
        
        try {
            scheduleService.updateSchedule(schedule);
            redirectAttributes.addFlashAttribute("message", 
                "Đã gửi yêu cầu thay đổi lịch! Admin sẽ xem xét và phản hồi.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }

        return "redirect:/teacher/schedules";
    }

    // Lấy lịch theo tuần (AJAX)
    @GetMapping("/weekly")
    @ResponseBody
    public List<Schedule> getWeeklySchedule(@RequestParam Long teacherId,
                                          @RequestParam String startDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = start.plusDays(6);
            return scheduleService.findTeacherScheduleByWeek(teacherId, start, end);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    // Lấy lịch theo tháng (AJAX)
    @GetMapping("/monthly")
    @ResponseBody
    public List<Schedule> getMonthlySchedule(@RequestParam Long teacherId,
                                           @RequestParam int year,
                                           @RequestParam int month) {
        try {
            return scheduleService.findTeacherScheduleByMonth(teacherId, year, month);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    // Xuất lịch giảng dạy
    @GetMapping("/export")
    public String exportSchedule(Model model,
                                @RequestParam(required = false) String format,
                                @RequestParam(required = false) String period) {
        User teacher = getCurrentTeacher();
        if (teacher == null) {
            return "redirect:/login";
        }

        // Mặc định xuất theo tuần hiện tại
        LocalDate startDate = LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1);
        LocalDate endDate = startDate.plusDays(6);
        
        if ("month".equals(period)) {
            startDate = LocalDate.now().withDayOfMonth(1);
            endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        } else if ("semester".equals(period)) {
            // Xuất theo học kỳ - có thể tùy chỉnh logic
            startDate = LocalDate.now().withMonth(1).withDayOfMonth(1);
            endDate = LocalDate.now().withMonth(12).withDayOfMonth(31);
        }

        List<Schedule> schedules = scheduleService.findTeacherScheduleByPeriod(
            teacher.getId(), startDate, endDate);

        model.addAttribute("teacher", teacher);
        model.addAttribute("schedules", schedules);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("period", period != null ? period : "week");

        // Trả về view export (có thể là PDF, Excel, etc.)
        return "teacher/teacher-schedule-export";
    }
}

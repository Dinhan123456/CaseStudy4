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
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/schedules")
public class AdminScheduleController {

    @Autowired
    private IScheduleService scheduleService;
    
    @Autowired
    private IClassService classService;
    
    @Autowired
    private ISubjectService subjectService;
    
    @Autowired
    private IUserService userService;
    
    @Autowired
    private IRoomService roomService;
    
    @Autowired
    private ITimeSlotService timeSlotService;

    // Kiểm tra quyền admin
    private User getCurrentAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            Optional<User> userOpt = userService.findByUsername(auth.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getRole() == Role.ADMIN) {
                    return user;
                }
            }
        }
        return null;
    }

    // Tìm User theo ID
    private User findUserById(Long id) {
        List<User> allUsers = userService.findAll();
        return allUsers.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    // Trang chính quản lý lịch
    @GetMapping
    public String adminSchedulePage(Model model) {
        User admin = getCurrentAdmin();
        if (admin == null) {
            return "redirect:/login";
        }

        // Lấy tất cả lịch đang hoạt động
        List<Schedule> schedules = scheduleService.findAllActiveSchedules();
        
        // Lấy dữ liệu cho form tạo lịch
        List<com.codegym.module4casestudy.model.Class> classes = classService.findAll();
        List<Subject> subjects = subjectService.findAll();
        List<User> teachers = userService.findByRole(Role.TEACHER);
        List<Room> rooms = roomService.findAllActive();
        List<TimeSlot> timeSlots = timeSlotService.findNonBreakTimeSlots();

        model.addAttribute("schedules", schedules);
        model.addAttribute("classes", classes);
        model.addAttribute("subjects", subjects);
        model.addAttribute("teachers", teachers);
        model.addAttribute("rooms", rooms);
        model.addAttribute("timeSlots", timeSlots);
        model.addAttribute("newSchedule", new Schedule());

        return "admin/admin-schedules";
    }

    // Tạo lịch mới
    @PostMapping("/create")
    public String createSchedule(@ModelAttribute("newSchedule") Schedule schedule,
                                @RequestParam("classId") Long classId,
                                @RequestParam("subjectId") Long subjectId,
                                @RequestParam("teacherId") Long teacherId,
                                @RequestParam("roomId") Long roomId,
                                @RequestParam("timeSlotId") Long timeSlotId,
                                RedirectAttributes redirectAttributes) {
        
        User admin = getCurrentAdmin();
        if (admin == null) {
            return "redirect:/login";
        }

        try {
            // Thiết lập các đối tượng liên quan
            Optional<com.codegym.module4casestudy.model.Class> classOpt = classService.findById(classId);
            Optional<Subject> subjectOpt = subjectService.findById(subjectId);
            User teacher = findUserById(teacherId);
            Room room = roomService.findById(roomId);
            TimeSlot timeSlot = timeSlotService.findById(timeSlotId);

            if (!classOpt.isPresent() || !subjectOpt.isPresent() || teacher == null || room == null || timeSlot == null) {
                redirectAttributes.addFlashAttribute("error", "Dữ liệu không hợp lệ!");
                return "redirect:/admin/schedules";
            }

            schedule.setClassEntity(classOpt.get());
            schedule.setSubject(subjectOpt.get());
            schedule.setTeacher(teacher);
            schedule.setRoom(room);
            schedule.setTimeSlot(timeSlot);
            schedule.setCreatedBy(admin);

            // Tạo lịch (có kiểm tra conflict)
            scheduleService.createSchedule(schedule);
            
            redirectAttributes.addFlashAttribute("message", "Tạo lịch học thành công!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }

        return "redirect:/admin/schedules";
    }

    // Xem chi tiết lịch
    @GetMapping("/{id}")
    public String viewSchedule(@PathVariable Long id, Model model) {
        User admin = getCurrentAdmin();
        if (admin == null) {
            return "redirect:/login";
        }

        Schedule schedule = scheduleService.findById(id);
        if (schedule == null) {
            return "redirect:/admin/schedules";
        }

        model.addAttribute("schedule", schedule);
        return "admin/admin-schedule-detail";
    }

    // Form chỉnh sửa lịch
    @GetMapping("/{id}/edit")
    public String editScheduleForm(@PathVariable Long id, Model model) {
        User admin = getCurrentAdmin();
        if (admin == null) {
            return "redirect:/login";
        }

        Schedule schedule = scheduleService.findById(id);
        if (schedule == null) {
            return "redirect:/admin/schedules";
        }

        // Lấy dữ liệu cho form
        List<com.codegym.module4casestudy.model.Class> classes = classService.findAll();
        List<Subject> subjects = subjectService.findAll();
        List<User> teachers = userService.findByRole(Role.TEACHER);
        List<Room> rooms = roomService.findAllActive();
        List<TimeSlot> timeSlots = timeSlotService.findNonBreakTimeSlots();

        model.addAttribute("schedule", schedule);
        model.addAttribute("classes", classes);
        model.addAttribute("subjects", subjects);
        model.addAttribute("teachers", teachers);
        model.addAttribute("rooms", rooms);
        model.addAttribute("timeSlots", timeSlots);

        return "admin/admin-schedule-edit";
    }

    // Cập nhật lịch
    @PostMapping("/{id}/update")
    public String updateSchedule(@PathVariable Long id,
                                @ModelAttribute("schedule") Schedule schedule,
                                @RequestParam("classId") Long classId,
                                @RequestParam("subjectId") Long subjectId,
                                @RequestParam("teacherId") Long teacherId,
                                @RequestParam("roomId") Long roomId,
                                @RequestParam("timeSlotId") Long timeSlotId,
                                RedirectAttributes redirectAttributes) {
        
        User admin = getCurrentAdmin();
        if (admin == null) {
            return "redirect:/login";
        }

        try {
            Schedule existingSchedule = scheduleService.findById(id);
            if (existingSchedule == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy lịch học!");
                return "redirect:/admin/schedules";
            }

            // Thiết lập các đối tượng liên quan
            Optional<com.codegym.module4casestudy.model.Class> classOpt = classService.findById(classId);
            Optional<Subject> subjectOpt = subjectService.findById(subjectId);
            User teacher = findUserById(teacherId);
            Room room = roomService.findById(roomId);
            TimeSlot timeSlot = timeSlotService.findById(timeSlotId);

            if (!classOpt.isPresent() || !subjectOpt.isPresent() || teacher == null || room == null || timeSlot == null) {
                redirectAttributes.addFlashAttribute("error", "Dữ liệu không hợp lệ!");
                return "redirect:/admin/schedules/" + id + "/edit";
            }

            // Cập nhật thông tin
            existingSchedule.setClassEntity(classOpt.get());
            existingSchedule.setSubject(subjectOpt.get());
            existingSchedule.setTeacher(teacher);
            existingSchedule.setRoom(room);
            existingSchedule.setTimeSlot(timeSlot);
            existingSchedule.setDayOfWeek(schedule.getDayOfWeek());
            existingSchedule.setStartDate(schedule.getStartDate());
            existingSchedule.setEndDate(schedule.getEndDate());
            existingSchedule.setNotes(schedule.getNotes());

            // Cập nhật lịch (có kiểm tra conflict)
            scheduleService.updateSchedule(existingSchedule);
            
            redirectAttributes.addFlashAttribute("message", "Cập nhật lịch học thành công!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/schedules/" + id + "/edit";
        }

        return "redirect:/admin/schedules/" + id;
    }

    // Xác nhận lịch
    @PostMapping("/{id}/confirm")
    public String confirmSchedule(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User admin = getCurrentAdmin();
        if (admin == null) {
            return "redirect:/login";
        }

        Schedule schedule = scheduleService.confirmSchedule(id);
        if (schedule != null) {
            redirectAttributes.addFlashAttribute("message", "Đã xác nhận lịch học!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy lịch học!");
        }

        return "redirect:/admin/schedules";
    }

    // Hủy lịch
    @PostMapping("/{id}/cancel")
    public String cancelSchedule(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User admin = getCurrentAdmin();
        if (admin == null) {
            return "redirect:/login";
        }

        Schedule schedule = scheduleService.cancelSchedule(id);
        if (schedule != null) {
            redirectAttributes.addFlashAttribute("message", "Đã hủy lịch học!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy lịch học!");
        }

        return "redirect:/admin/schedules";
    }

    // Xóa lịch
    @PostMapping("/{id}/delete")
    public String deleteSchedule(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User admin = getCurrentAdmin();
        if (admin == null) {
            return "redirect:/login";
        }

        try {
            scheduleService.deleteById(id);
            redirectAttributes.addFlashAttribute("message", "Đã xóa lịch học!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa: " + e.getMessage());
        }

        return "redirect:/admin/schedules";
    }

    // Kiểm tra trùng lịch (AJAX)
    @PostMapping("/check-conflict")
    @ResponseBody
    public String checkConflict(@RequestParam Long teacherId,
                               @RequestParam Long roomId,
                               @RequestParam Long classId,
                               @RequestParam Integer dayOfWeek,
                               @RequestParam Long timeSlotId,
                               @RequestParam String startDate,
                               @RequestParam(required = false) String endDate) {
        
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = endDate != null && !endDate.isEmpty() ? LocalDate.parse(endDate) : null;
            
            boolean hasConflict = scheduleService.hasConflictingSchedule(
                teacherId, roomId, classId, dayOfWeek, timeSlotId, start, end);
            
            return hasConflict ? "conflict" : "ok";
            
        } catch (Exception e) {
            return "error";
        }
    }

    // Lấy lịch theo bộ lọc (AJAX)
    @GetMapping("/filter")
    @ResponseBody
    public List<Schedule> filterSchedules(@RequestParam(required = false) Long teacherId,
                                         @RequestParam(required = false) Long classId,
                                         @RequestParam(required = false) Long roomId,
                                         @RequestParam(required = false) Integer dayOfWeek) {
        
        if (teacherId != null) {
            return dayOfWeek != null ? 
                   scheduleService.findByTeacherAndDay(teacherId, dayOfWeek) :
                   scheduleService.findByTeacherId(teacherId);
        }
        
        if (classId != null) {
            return dayOfWeek != null ?
                   scheduleService.findByClassAndDay(classId, dayOfWeek) :
                   scheduleService.findByClassId(classId);
        }
        
        if (roomId != null) {
            return scheduleService.findByRoomId(roomId);
        }
        
        if (dayOfWeek != null) {
            return scheduleService.findByDayOfWeek(dayOfWeek);
        }
        
        return scheduleService.findAllActiveSchedules();
    }
}

package com.codegym.module4casestudy.controller;

import com.codegym.module4casestudy.model.User;
import com.codegym.module4casestudy.model.Class;
import com.codegym.module4casestudy.model.Subject;
import com.codegym.module4casestudy.model.ClassSubject;
import com.codegym.module4casestudy.model.RegistrationPeriod;
import com.codegym.module4casestudy.repository.ClassSubjectRepository;
import com.codegym.module4casestudy.service.IClassService;
import com.codegym.module4casestudy.service.ISubjectService;
import com.codegym.module4casestudy.service.IUserService;
import com.codegym.module4casestudy.service.IRegistrationPeriodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.lang.reflect.Method;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired private IUserService userService;
    @Autowired private IClassService classService;
    @Autowired private ISubjectService subjectService;
    @Autowired private IRegistrationPeriodService registrationPeriodService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ClassSubjectRepository classSubjectRepository;

    /* ===================== Helpers ===================== */

    private User getCurrentStudent() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(String.valueOf(auth.getPrincipal()))) {
            return null;
        }
        return userService.findByUsername(auth.getName()).orElse(null);
    }

    private Object callGetter(Object target, String... names) {
        if (target == null) return null;
        for (String n : names) {
            try {
                Method m = target.getClass().getMethod(n);
                return m.invoke(target);
            } catch (Exception ignored) { }
        }
        return null;
    }

    /* ---- Subject maps (từ bảng liên kết ClassSubject) ---- */
    private Map<Long, String> buildSubjectNameMap(List<Class> classes) {
        Map<Long, String> map = new HashMap<>();
        for (Class c : classes) {
            List<ClassSubject> links = classSubjectRepository.findByClassEntityId(c.getId());
            String names = links.stream()
                    .map(cs -> cs.getSubject() != null ? cs.getSubject().getName() : null)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(", "));
            map.put(c.getId(), names);
        }
        return map;
    }

    private Map<Long, Integer> buildSubjectCreditsMap(List<Class> classes) {
        Map<Long, Integer> map = new HashMap<>();
        for (Class c : classes) {
            List<ClassSubject> links = classSubjectRepository.findByClassEntityId(c.getId());
            int credits = links.stream()
                    .map(cs -> {
                        Subject s = cs.getSubject();
                        return (s != null && s.getCredits() != null) ? s.getCredits() : 0;
                    })
                    .reduce(0, Integer::sum);
            map.put(c.getId(), credits);
        }
        return map;
    }

    /* ---- Các map khác: cố gắng lấy bằng reflection, không có thì để rỗng/0 ---- */
    private Map<Long, String> buildTeacherNameMap(List<Class> classes) {
        Map<Long, String> map = new HashMap<>();
        for (Class c : classes) {
            Object teacher = callGetter(c, "getTeacher", "getInstructor", "getLecturer");
            Object name = teacher != null
                    ? callGetter(teacher, "getFullName", "getName", "getUsername")
                    : null;
            map.put(c.getId(), name != null ? String.valueOf(name) : "");
        }
        return map;
    }

    private Map<Long, String> buildScheduleMap(List<Class> classes) {
        Map<Long, String> map = new HashMap<>();
        for (Class c : classes) {
            Object schedule = callGetter(c, "getSchedule", "getTime", "getTimetable", "getTimeTable");
            map.put(c.getId(), schedule != null ? String.valueOf(schedule) : "");
        }
        return map;
    }

    private Map<Long, String> buildRoomMap(List<Class> classes) {
        Map<Long, String> map = new HashMap<>();
        for (Class c : classes) {
            Object room = callGetter(c, "getRoom", "getClassroom", "getRoomName");
            map.put(c.getId(), room != null ? String.valueOf(room) : "");
        }
        return map;
    }

    private Map<Long, Integer> buildStudentCountMap(List<Class> classes) {
        Map<Long, Integer> map = new HashMap<>();
        for (Class c : classes) {
            Object students = callGetter(c, "getStudents", "getStudentList", "getEnrolledStudents");
            Integer count = 0;
            if (students instanceof Collection<?>) {
                count = ((Collection<?>) students).size();
            } else {
                Object n = callGetter(c, "getStudentCount", "getEnrolled", "getCurrentSize");
                if (n instanceof Number) count = ((Number) n).intValue();
            }
            map.put(c.getId(), count);
        }
        return map;
    }

    private Map<Long, Integer> buildCapacityMap(List<Class> classes) {
        Map<Long, Integer> map = new HashMap<>();
        for (Class c : classes) {
            Object cap = callGetter(c, "getCapacity", "getMaxStudents", "getLimit", "getQuota");
            map.put(c.getId(), cap instanceof Number ? ((Number) cap).intValue() : 40); // mặc định 40
        }
        return map;
    }

    private void addClassMapsToModel(Model model, List<Class> classes) {
        // Gộp và loại trùng theo ID để build map 1 lần
        Map<Long, Class> byId = new LinkedHashMap<>();
        for (Class cl : classes) byId.put(cl.getId(), cl);
        List<Class> list = new ArrayList<>(byId.values());

        model.addAttribute("subjectNameMap",   buildSubjectNameMap(list));
        model.addAttribute("subjectCreditsMap",buildSubjectCreditsMap(list));
        model.addAttribute("teacherNameMap",   buildTeacherNameMap(list));
        model.addAttribute("scheduleMap",      buildScheduleMap(list));
        model.addAttribute("roomMap",          buildRoomMap(list));
        model.addAttribute("studentCountMap",  buildStudentCountMap(list));
        model.addAttribute("capacityMap",      buildCapacityMap(list));
    }

    /* ===================== Endpoints ===================== */

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        User student = userService.findByUsername(principal.getName()).orElse(null);
        if (student == null) return "redirect:/login";

        List<Class> recentClasses = classService.findRecentClassesForStudentWithFetch(student.getId());
        model.addAttribute("recentClasses", recentClasses != null ? recentClasses : Collections.emptyList());
        return "student/student-panel";
    }

    @GetMapping("/dashboard-simple")
    public String showStudentDashboardSimple(Model model) {
        try {
            User student = getCurrentStudent();
            if (student == null) {
                model.addAttribute("error", "Không tìm thấy thông tin sinh viên! Vui lòng đăng nhập lại.");
                student = new User(); student.setUsername("Unknown"); student.setFullName("Không xác định");
            }
            List<Class> classes = classService.findAll();
            model.addAttribute("student", student);
            model.addAttribute("totalClasses", classes.size());
            model.addAttribute("recentClasses", classes.size() > 3 ? classes.subList(0,3) : classes);
            return "student/student-panel-simple";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            User dummy = new User(); dummy.setUsername("Unknown"); dummy.setFullName("Không xác định");
            model.addAttribute("student", dummy);
            model.addAttribute("totalClasses", 0);
            model.addAttribute("recentClasses", new ArrayList<>());
            return "student/student-panel-simple";
        }
    }

    @GetMapping("/dashboard-fixed")
    public String showStudentDashboardFixed(Model model) {
        try {
            User student = getCurrentStudent();
            if (student == null) {
                model.addAttribute("error", "Không tìm thấy thông tin sinh viên! Vui lòng đăng nhập lại.");
                student = new User(); student.setUsername("Unknown"); student.setFullName("Không xác định");
            }
            List<Class> classes = classService.findAll();
            model.addAttribute("student", student);
            model.addAttribute("totalClasses", classes.size());
            model.addAttribute("recentClasses", classes.size() > 3 ? classes.subList(0,3) : classes);
            return "student/student-panel-fixed";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            User dummy = new User(); dummy.setUsername("Unknown"); dummy.setFullName("Không xác định");
            model.addAttribute("student", dummy);
            model.addAttribute("totalClasses", 0);
            model.addAttribute("recentClasses", new ArrayList<>());
            return "student/student-panel-fixed";
        }
    }

    @GetMapping("/debug")
    public String debugStudent(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User student = getCurrentStudent();
        model.addAttribute("auth", auth);
        model.addAttribute("student", student);
        model.addAttribute("authName", auth != null ? auth.getName() : "null");
        model.addAttribute("authDetails", auth != null ? auth.getDetails() : "null");
        model.addAttribute("authAuthorities", auth != null ? auth.getAuthorities() : "null");
        return "student/student-debug";
    }

    @GetMapping("/test") public String testStudent(Model model) {
        model.addAttribute("message", "Test endpoint working!");
        return "student/student-test";
    }

    @GetMapping("/test-simple") public String testSimple() {
        return "student/student-test-simple";
    }

    @GetMapping("/session")
    public String checkSession(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User student = getCurrentStudent();
        model.addAttribute("auth", auth);
        model.addAttribute("student", student);
        model.addAttribute("authName", auth != null ? auth.getName() : "null");
        model.addAttribute("authDetails", auth != null ? auth.getDetails() : "null");
        model.addAttribute("authAuthorities", auth != null ? auth.getAuthorities() : "null");
        return "student/student-session";
    }

    @GetMapping("/profile")
    public String showStudentProfile(Model model) {
        User student = getCurrentStudent();
        if (student == null) {
            model.addAttribute("error", "Không tìm thấy thông tin sinh viên! Vui lòng đăng nhập lại.");
            student = new User(); student.setUsername("Unknown"); student.setFullName("Không xác định");
        }
        model.addAttribute("student", student);
        return "student/student-profile";
    }

    @PostMapping("/update-profile")
    public String updateStudentProfile(@RequestParam String email,
                                       @RequestParam String phone,
                                       @RequestParam String fullName,
                                       @RequestParam(required = false) String passwordOld,
                                       @RequestParam(required = false) String password,
                                       RedirectAttributes redirect) {
        try {
            User student = getCurrentStudent();
            if (student == null) {
                redirect.addFlashAttribute("error", "Không tìm thấy thông tin sinh viên!");
                return "redirect:/student/profile";
            }
            student.setEmail(email);
            student.setPhone(phone);
            student.setFullName(fullName);

            if (password != null && !password.trim().isEmpty()) {
                if (passwordOld == null || passwordOld.trim().isEmpty()) {
                    redirect.addFlashAttribute("error", "Vui lòng nhập mật khẩu cũ!");
                    return "redirect:/student/profile";
                }
                if (!passwordEncoder.matches(passwordOld, student.getPassword())) {
                    redirect.addFlashAttribute("error", "Mật khẩu cũ không đúng!");
                    return "redirect:/student/profile";
                }
                student.setPassword(passwordEncoder.encode(password));
            }

            userService.save(student);
            redirect.addFlashAttribute("message", "Cập nhật thông tin thành công!");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Có lỗi xảy ra khi cập nhật thông tin!");
        }
        return "redirect:/student/profile";
    }

    @GetMapping("/classes")
    public String showStudentClasses(Model model) {
        User student = getCurrentStudent();
        if (student == null) return "redirect:/login";

        List<Class> studentClasses = classService.findClassesByStudentId(student.getId());

        Set<Long> enrolledIds = studentClasses.stream().map(Class::getId).collect(Collectors.toSet());
        List<Class> availableClasses = classService.findAll().stream()
                .filter(c -> !enrolledIds.contains(c.getId()))
                .collect(Collectors.toList());

        // Build maps dùng cho cả 2 danh sách
        List<Class> all = new ArrayList<>();
        all.addAll(studentClasses);
        all.addAll(availableClasses);
        addClassMapsToModel(model, all);

        model.addAttribute("student", student);
        model.addAttribute("studentClasses", studentClasses);
        model.addAttribute("availableClasses", availableClasses);
        return "student/student-classes";
    }

    @GetMapping("/classes/{classId}")
    public String showClassDetails(@PathVariable Long classId, Model model) {
        User student = getCurrentStudent();
        if (student == null) {
            model.addAttribute("error", "Không tìm thấy thông tin sinh viên! Vui lòng đăng nhập lại.");
            return "redirect:/login";
        }
        Class studentClass = classService.findById(classId).orElse(null);
        if (studentClass == null) {
            model.addAttribute("error", "Không tìm thấy lớp học!");
            return "redirect:/student/classes";
        }
        boolean isEnrolled = classService.findClassesByStudentId(student.getId())
                .stream().anyMatch(c -> c.getId().equals(classId));

        model.addAttribute("student", student);
        model.addAttribute("class", studentClass);
        model.addAttribute("isEnrolled", isEnrolled);
        return "student/student-class-details";
    }

    @GetMapping("/grades")
    public String showStudentGrades(
            @RequestParam(value = "semester", required = false) String semester,
            @RequestParam(value = "subjectId", required = false) Long subjectId,
            @RequestParam(value = "status", required = false) String status,
            Model model) {
        User student = getCurrentStudent();
        if (student == null) {
            model.addAttribute("error", "Không tìm thấy thông tin sinh viên! Vui lòng đăng nhập lại.");
            student = new User(); student.setUsername("Unknown"); student.setFullName("Không xác định");
        }
        List<Class> studentClasses = classService.findClassesByStudentId(student.getId());
        List<Subject> subjects = subjectService.findActiveSubjects();

        // Lấy tất cả điểm của sinh viên
        List<com.codegym.module4casestudy.model.Grade> allGrades = classService.getGradesByStudentId(student.getId());
        // Lọc theo học kỳ
        if (semester != null && !semester.isEmpty()) {
            allGrades = allGrades.stream().filter(g -> semester.equals(g.getSemester())).collect(Collectors.toList());
        }
        // Lọc theo môn học
        if (subjectId != null) {
            allGrades = allGrades.stream().filter(g -> g.getSubject() != null && subjectId.equals(g.getSubject().getId())).collect(Collectors.toList());
        }
        // Lọc theo trạng thái (pass/fail/studying)
        if (status != null && !status.isEmpty()) {
            allGrades = allGrades.stream().filter(g -> {
                if ("pass".equals(status)) return g.getAverageGrade() != null && g.getAverageGrade() >= 5.5;
                if ("fail".equals(status)) return g.getAverageGrade() != null && g.getAverageGrade() < 5.5;
                if ("studying".equals(status)) return g.getAverageGrade() == null;
                return true;
            }).collect(Collectors.toList());
        }

        model.addAttribute("student", student);
        model.addAttribute("classes", studentClasses);
        model.addAttribute("subjects", subjects);
        model.addAttribute("grades", allGrades);
        model.addAttribute("selectedSemester", semester);
        model.addAttribute("selectedSubjectId", subjectId);
        model.addAttribute("selectedStatus", status);
        return "student/student-grades";
    }

    @GetMapping("/schedule")
    public String showStudentSchedule(Model model) {
        User student = getCurrentStudent();
        if (student == null) {
            model.addAttribute("error", "Không tìm thấy thông tin sinh viên! Vui lòng đăng nhập lại.");
            student = new User(); student.setUsername("Unknown"); student.setFullName("Không xác định");
        }
        List<Class> studentClasses = classService.findClassesByStudentId(student.getId());
        model.addAttribute("student", student);
        model.addAttribute("classes", studentClasses);
        return "student/student-schedule";
    }

    @GetMapping("/course-registration")
    public String showCourseRegistration(Model model) {
        User student = getCurrentStudent();
        if (student == null) {
            model.addAttribute("error", "Không tìm thấy thông tin sinh viên! Vui lòng đăng nhập lại.");
            return "redirect:/login";
        }
        
        // Kiểm tra trạng thái đăng ký
        boolean canRegister = registrationPeriodService.canRegisterNow();
        RegistrationPeriod currentPeriod = registrationPeriodService.getCurrentActivePeriod();
        
        // Lấy danh sách lớp học mà sinh viên đã đăng ký
        List<Class> enrolledClasses = classService.findClassesByStudentId(student.getId());
        Set<Long> enrolledIds = enrolledClasses.stream().map(Class::getId).collect(Collectors.toSet());
        
        // Lấy danh sách tất cả lớp học có thể đăng ký
        List<Class> availableClasses = classService.findAll().stream()
                .filter(c -> !enrolledIds.contains(c.getId()))
                .collect(Collectors.toList());
        
        // Lấy danh sách môn học
        List<Subject> subjects = subjectService.findActiveSubjects();
        
        model.addAttribute("student", student);
        model.addAttribute("enrolledClasses", enrolledClasses);
        model.addAttribute("availableClasses", availableClasses);
        model.addAttribute("subjects", subjects);
        model.addAttribute("canRegister", canRegister);
        model.addAttribute("currentPeriod", currentPeriod);
        model.addAttribute("registrationStatus", canRegister ? "OPEN" : "CLOSED");
        
        // Thêm các map để hiển thị thông tin
        List<Class> allClasses = new ArrayList<>();
        allClasses.addAll(enrolledClasses);
        allClasses.addAll(availableClasses);
        addClassMapsToModel(model, allClasses);
        
        return "student/student-course-registration";
    }

    @GetMapping("/schedule/{scheduleId}/detail")
    public String showScheduleDetail(@PathVariable Long scheduleId, Model model) {
        User student = getCurrentStudent();
        if (student == null) {
            model.addAttribute("error", "Không tìm thấy thông tin sinh viên! Vui lòng đăng nhập lại.");
            return "redirect:/login";
        }
        
        // Tìm lớp học theo ID (giả sử scheduleId là classId)
        Class selectedClass = classService.findById(scheduleId).orElse(null);
        if (selectedClass == null) {
            model.addAttribute("error", "Không tìm thấy thông tin lịch học!");
            return "redirect:/student/schedule";
        }
        
        // Kiểm tra xem sinh viên có đăng ký lớp này không
        List<Class> studentClasses = classService.findClassesByStudentId(student.getId());
        boolean isEnrolled = studentClasses.stream().anyMatch(c -> c.getId().equals(scheduleId));
        
        if (!isEnrolled) {
            model.addAttribute("error", "Bạn chưa đăng ký lớp học này!");
            return "redirect:/student/schedule";
        }
        
        model.addAttribute("student", student);
        model.addAttribute("schedule", selectedClass);
        model.addAttribute("classmates", new ArrayList<>()); // Placeholder - cần implement
        model.addAttribute("myGrades", null); // Placeholder - cần implement
        model.addAttribute("attendanceRate", 85); // Placeholder
        model.addAttribute("completedSessions", 12); // Placeholder
        model.addAttribute("totalSessions", 15); // Placeholder
        model.addAttribute("daysRemaining", 45); // Placeholder
        
        return "student/student-schedule-detail";
    }

    @PostMapping("/classes/{classId}/register")
    public String registerClass(@PathVariable Long classId, RedirectAttributes redirect) {
        User student = getCurrentStudent();
        if (student == null) return "redirect:/login";

        try {
            // Kiểm tra trạng thái đăng ký trước tiên
            String validationError = registrationPeriodService.validateRegistrationAction("đăng ký lớp học");
            if (validationError != null) {
                redirect.addFlashAttribute("error", validationError);
                return "redirect:/student/course-registration";
            }
            
            Class target = classService.findById(classId).orElse(null);
            if (target == null) {
                redirect.addFlashAttribute("error", "Không tìm thấy lớp học!");
                return "redirect:/student/course-registration";
            }
            
            // Kiểm tra sinh viên đã đăng ký chưa
            boolean exists = classService.findClassesByStudentId(student.getId())
                    .stream().anyMatch(c -> c.getId().equals(classId));
            if (exists) {
                redirect.addFlashAttribute("error", "Bạn đã đăng ký lớp này rồi!");
                return "redirect:/student/course-registration";
            }
            
            // Kiểm tra capacity trước khi đăng ký
            if (!classService.canAddStudentToClass(classId)) {
                redirect.addFlashAttribute("error", "Lớp học đã đầy! Không thể đăng ký thêm.");
                return "redirect:/student/course-registration";
            }
            
            classService.addStudentToClass(classId, student.getId());
            redirect.addFlashAttribute("message", "Đăng ký lớp học thành công!");
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Có lỗi xảy ra khi đăng ký lớp: " + e.getMessage());
        }
        return "redirect:/student/course-registration";
    }

    @PostMapping("/classes/{classId}/unregister")
    public String unregisterClass(@PathVariable Long classId, RedirectAttributes redirect) {
        User student = getCurrentStudent();
        if (student == null) return "redirect:/login";

        try {
            // Kiểm tra trạng thái đăng ký trước tiên
            String validationError = registrationPeriodService.validateRegistrationAction("hủy đăng ký lớp học");
            if (validationError != null) {
                redirect.addFlashAttribute("error", validationError);
                return "redirect:/student/course-registration";
            }
            
            Class target = classService.findById(classId).orElse(null);
            if (target == null) {
                redirect.addFlashAttribute("error", "Không tìm thấy lớp học!");
                return "redirect:/student/course-registration";
            }
            
            boolean enrolled = classService.findClassesByStudentId(student.getId())
                    .stream().anyMatch(c -> c.getId().equals(classId));
            if (!enrolled) {
                redirect.addFlashAttribute("error", "Bạn chưa đăng ký lớp này!");
                return "redirect:/student/course-registration";
            }
            
            classService.removeStudentFromClass(classId, student.getId());
            redirect.addFlashAttribute("message", "Hủy đăng ký lớp học thành công!");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Có lỗi xảy ra khi hủy đăng ký lớp: " + e.getMessage());
        }
        return "redirect:/student/course-registration";
    }

    // JSON API endpoints for AJAX calls
    @PostMapping("/api/classes/{classId}/register")
    @ResponseBody
    public Map<String, Object> registerClassApi(@PathVariable Long classId) {
        Map<String, Object> response = new HashMap<>();
        
        User student = getCurrentStudent();
        if (student == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập!");
            return response;
        }

        try {
            // Kiểm tra trạng thái đăng ký trước tiên
            String validationError = registrationPeriodService.validateRegistrationAction("đăng ký lớp học");
            if (validationError != null) {
                response.put("success", false);
                response.put("message", validationError);
                return response;
            }
            
            Class target = classService.findById(classId).orElse(null);
            if (target == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy lớp học!");
                return response;
            }
            
            // Kiểm tra đã đăng ký chưa
            List<Class> studentClasses = classService.findClassesByStudentId(student.getId());
            boolean alreadyEnrolled = studentClasses.stream().anyMatch(c -> c.getId().equals(classId));
            if (alreadyEnrolled) {
                response.put("success", false);
                response.put("message", "Bạn đã đăng ký lớp này rồi!");
                return response;
            }
            
            // Kiểm tra sức chứa lớp
            if (!classService.canAddStudentToClass(classId)) {
                response.put("success", false);
                response.put("message", "Lớp học đã đầy!");
                return response;
            }
            
            classService.addStudentToClass(classId, student.getId());
            response.put("success", true);
            response.put("message", "Đăng ký lớp học thành công!");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi đăng ký lớp: " + e.getMessage());
        }
        
        return response;
    }

    @PostMapping("/api/classes/{classId}/unregister")
    @ResponseBody  
    public Map<String, Object> unregisterClassApi(@PathVariable Long classId) {
        Map<String, Object> response = new HashMap<>();
        
        User student = getCurrentStudent();
        if (student == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập!");
            return response;
        }

        try {
            // Kiểm tra trạng thái đăng ký trước tiên
            String validationError = registrationPeriodService.validateRegistrationAction("hủy đăng ký lớp học");
            if (validationError != null) {
                response.put("success", false);
                response.put("message", validationError);
                return response;
            }
            
            Class target = classService.findById(classId).orElse(null);
            if (target == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy lớp học!");
                return response;
            }
            
            boolean enrolled = classService.findClassesByStudentId(student.getId())
                    .stream().anyMatch(c -> c.getId().equals(classId));
            if (!enrolled) {
                response.put("success", false);
                response.put("message", "Bạn chưa đăng ký lớp này!");
                return response;
            }
            
            classService.removeStudentFromClass(classId, student.getId());
            response.put("success", true);
            response.put("message", "Hủy đăng ký lớp học thành công!");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi hủy đăng ký lớp: " + e.getMessage());
        }
        
        return response;
    }
}

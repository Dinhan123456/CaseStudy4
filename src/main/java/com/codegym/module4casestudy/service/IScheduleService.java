package com.codegym.module4casestudy.service;

import com.codegym.module4casestudy.model.Schedule;
import com.codegym.module4casestudy.model.User;
import java.time.LocalDate;
import java.util.List;

public interface IScheduleService {
    
    // CRUD cơ bản
    List<Schedule> findAll();
    Schedule findById(Long id);
    Schedule save(Schedule schedule);
    void deleteById(Long id);
    
    // Tìm lịch theo giảng viên
    List<Schedule> findByTeacher(User teacher);
    List<Schedule> findByTeacherId(Long teacherId);
    
    // Tìm lịch theo lớp học
    List<Schedule> findByClassId(Long classId);
    
    // Tìm lịch theo môn học
    List<Schedule> findBySubjectId(Long subjectId);
    
    // Tìm lịch theo phòng học
    List<Schedule> findByRoomId(Long roomId);
    
    // Tìm lịch theo sinh viên (thông qua lớp họ đăng ký)
    List<Schedule> findByStudentId(Long studentId);
    
    // Tìm lịch theo ngày trong tuần
    List<Schedule> findByDayOfWeek(Integer dayOfWeek);
    
    // Tìm lịch theo giảng viên và ngày
    List<Schedule> findByTeacherAndDay(Long teacherId, Integer dayOfWeek);
    
    // Tìm lịch theo lớp và ngày
    List<Schedule> findByClassAndDay(Long classId, Integer dayOfWeek);
    
    // Tìm lịch trong khoảng thời gian
    List<Schedule> findSchedulesInDateRange(LocalDate startDate, LocalDate endDate);
    
    // Lấy lịch toàn hệ thống cho admin
    List<Schedule> findAllActiveSchedules();
    
    // Tìm lịch theo trạng thái
    List<Schedule> findByStatus(Schedule.ScheduleStatus status);
    
    // Kiểm tra trùng lịch
    boolean hasConflictingSchedule(Long teacherId, Long roomId, Long classId, 
                                   Integer dayOfWeek, Long timeSlotId, 
                                   LocalDate startDate, LocalDate endDate);
    
    // Kiểm tra trùng lịch giảng viên
    List<Schedule> findConflictingTeacherSchedules(Long teacherId, Integer dayOfWeek, 
                                                   Long timeSlotId, LocalDate startDate, LocalDate endDate);
    
    // Kiểm tra trùng lịch phòng học
    List<Schedule> findConflictingRoomSchedules(Long roomId, Integer dayOfWeek, 
                                                Long timeSlotId, LocalDate startDate, LocalDate endDate);
    
    // Kiểm tra trùng lịch lớp học
    List<Schedule> findConflictingClassSchedules(Long classId, Integer dayOfWeek, 
                                                 Long timeSlotId, LocalDate startDate, LocalDate endDate);
    
    // Tạo lịch học mới (kiểm tra conflict)
    Schedule createSchedule(Schedule schedule) throws Exception;
    
    // Cập nhật lịch học (kiểm tra conflict)
    Schedule updateSchedule(Schedule schedule) throws Exception;
    
    // Xác nhận lịch học
    Schedule confirmSchedule(Long scheduleId);
    
    // Hủy lịch học
    Schedule cancelSchedule(Long scheduleId);
    
    // Hoàn thành lịch học
    Schedule completeSchedule(Long scheduleId);
    
    // Các phương thức cho TeacherScheduleController
    List<Schedule> findTeacherScheduleByWeek(Long teacherId, LocalDate startDate, LocalDate endDate);
    List<Schedule> findPendingSchedulesByTeacher(Long teacherId);
    List<Schedule> findTeacherScheduleByMonth(Long teacherId, int year, int month);
    List<Schedule> findTeacherScheduleByPeriod(Long teacherId, LocalDate startDate, LocalDate endDate);
    
    // Các phương thức cho StudentScheduleController
    List<Schedule> findStudentScheduleByWeek(Long studentId, LocalDate startDate, LocalDate endDate);
    List<Schedule> findAvailableSchedulesForRegistration();
    List<Schedule> findAvailableSchedulesBySubject(Long subjectId);
    boolean hasStudentScheduleConflict(Long studentId, Integer dayOfWeek, Long timeSlotId, 
                                      LocalDate startDate, LocalDate endDate);
    boolean registerStudentForSchedule(Long studentId, Long scheduleId);
    boolean unregisterStudentFromSchedule(Long studentId, Long scheduleId);
}

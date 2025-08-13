package com.codegym.module4casestudy.service;

import com.codegym.module4casestudy.model.Schedule;
import com.codegym.module4casestudy.model.ScheduleStatus;
import com.codegym.module4casestudy.model.ScheduleStatus;
import com.codegym.module4casestudy.model.User;
import com.codegym.module4casestudy.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ScheduleServiceImpl implements IScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Override
    public List<Schedule> findAll() {
        return scheduleRepository.findAll();
    }

    @Override
    public Schedule findById(Long id) {
        Optional<Schedule> schedule = scheduleRepository.findById(id);
        return schedule.orElse(null);
    }

    @Override
    public Schedule save(Schedule schedule) {
        // Không set createdAt/updatedAt vì không có setter
        return scheduleRepository.save(schedule);
    }

    @Override
    public void deleteById(Long id) {
        // Soft delete - chỉ đánh dấu inactive
        Schedule schedule = findById(id);
        if (schedule != null) {
            schedule.setActive(false);
            save(schedule);
        }
    }

    @Override
    public List<Schedule> findByTeacher(User teacher) {
        return scheduleRepository.findByTeacherAndActiveTrue(teacher);
    }

    @Override
    public List<Schedule> findByTeacherId(Long teacherId) {
        return scheduleRepository.findByTeacherIdAndActiveTrue(teacherId);
    }

    @Override
    public List<Schedule> findByClassId(Long classId) {
        return scheduleRepository.findByClassId(classId);
    }

    @Override
    public List<Schedule> findBySubjectId(Long subjectId) {
        return scheduleRepository.findBySubjectId(subjectId);
    }

    @Override
    public List<Schedule> findByRoomId(Long roomId) {
        return scheduleRepository.findByRoomId(roomId);
    }

    @Override
    public List<Schedule> findByStudentId(Long studentId) {
        return scheduleRepository.findByStudentId(studentId);
    }

    @Override
    public List<Schedule> findByDayOfWeek(Integer dayOfWeek) {
        return scheduleRepository.findByDayOfWeekAndActiveTrue(dayOfWeek);
    }

    @Override
    public List<Schedule> findByTeacherAndDay(Long teacherId, Integer dayOfWeek) {
        return scheduleRepository.findByTeacherAndDay(teacherId, dayOfWeek);
    }

    @Override
    public List<Schedule> findByClassAndDay(Long classId, Integer dayOfWeek) {
        return scheduleRepository.findByClassAndDay(classId, dayOfWeek);
    }

    @Override
    public List<Schedule> findSchedulesInDateRange(LocalDate startDate, LocalDate endDate) {
        return scheduleRepository.findSchedulesInDateRange(startDate, endDate);
    }

    @Override
    public List<Schedule> findAllActiveSchedules() {
        return scheduleRepository.findAllActiveSchedules();
    }

    @Override
    public List<Schedule> findByStatus(ScheduleStatus status) {
        return scheduleRepository.findByStatus(status);
    }

    @Override
    public boolean hasConflictingSchedule(Long teacherId, Long roomId, Long classId, 
                                         Integer dayOfWeek, Long timeSlotId, 
                                         LocalDate startDate, LocalDate endDate) {
        
        // Kiểm tra conflict giảng viên
        if (teacherId != null) {
            List<Schedule> teacherConflicts = findConflictingTeacherSchedules(
                teacherId, dayOfWeek, timeSlotId, startDate, endDate);
            if (!teacherConflicts.isEmpty()) {
                return true;
            }
        }
        
        // Kiểm tra conflict phòng học
        if (roomId != null) {
            List<Schedule> roomConflicts = findConflictingRoomSchedules(
                roomId, dayOfWeek, timeSlotId, startDate, endDate);
            if (!roomConflicts.isEmpty()) {
                return true;
            }
        }
        
        // Kiểm tra conflict lớp học
        if (classId != null) {
            List<Schedule> classConflicts = findConflictingClassSchedules(
                classId, dayOfWeek, timeSlotId, startDate, endDate);
            if (!classConflicts.isEmpty()) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public List<Schedule> findConflictingTeacherSchedules(Long teacherId, Integer dayOfWeek, 
                                                         Long timeSlotId, LocalDate startDate, LocalDate endDate) {
        return scheduleRepository.findConflictingTeacherSchedules(teacherId, dayOfWeek, timeSlotId, startDate, endDate);
    }

    @Override
    public List<Schedule> findConflictingRoomSchedules(Long roomId, Integer dayOfWeek, 
                                                      Long timeSlotId, LocalDate startDate, LocalDate endDate) {
        return scheduleRepository.findConflictingRoomSchedules(roomId, dayOfWeek, timeSlotId, startDate, endDate);
    }

    @Override
    public List<Schedule> findConflictingClassSchedules(Long classId, Integer dayOfWeek, 
                                                       Long timeSlotId, LocalDate startDate, LocalDate endDate) {
        return scheduleRepository.findConflictingClassSchedules(classId, dayOfWeek, timeSlotId, startDate, endDate);
    }

    @Override
    public Schedule createSchedule(Schedule schedule) throws Exception {
        // Kiểm tra conflict trước khi tạo
        boolean hasConflict = hasConflictingSchedule(
            schedule.getTeacher().getId(),
            schedule.getRoom().getId(),
            schedule.getClassEntity().getId(),
            schedule.getDayOfWeek() != null ? schedule.getDayOfWeek().ordinal() : null,
            schedule.getTimeSlot().getId(),
            schedule.getStartDate(),
            schedule.getEndDate()
        );
        
        if (hasConflict) {
            throw new Exception("Lịch học bị trùng! Vui lòng chọn thời gian, phòng học hoặc giảng viên khác.");
        }
        
        schedule.setStatus(ScheduleStatus.PENDING);
        return save(schedule);
    }

    @Override
    public Schedule updateSchedule(Schedule schedule) throws Exception {
        Schedule existingSchedule = findById(schedule.getId());
        if (existingSchedule == null) {
            throw new Exception("Không tìm thấy lịch học!");
        }
        
        // Kiểm tra conflict trước khi cập nhật (loại trừ chính nó)
        boolean hasConflict = hasConflictingSchedule(
            schedule.getTeacher().getId(),
            schedule.getRoom().getId(),
            schedule.getClassEntity().getId(),
            schedule.getDayOfWeek() != null ? schedule.getDayOfWeek().ordinal() : null,
            schedule.getTimeSlot().getId(),
            schedule.getStartDate(),
            schedule.getEndDate()
        );
        
        if (hasConflict) {
            throw new Exception("Lịch học bị trùng! Vui lòng chọn thời gian, phòng học hoặc giảng viên khác.");
        }
        
        return save(schedule);
    }

    @Override
    public Schedule confirmSchedule(Long scheduleId) {
        Schedule schedule = findById(scheduleId);
        if (schedule != null) {
            schedule.setStatus(ScheduleStatus.CONFIRMED);
            return save(schedule);
        }
        return null;
    }

    @Override
    public Schedule cancelSchedule(Long scheduleId) {
        Schedule schedule = findById(scheduleId);
        if (schedule != null) {
            schedule.setStatus(ScheduleStatus.CANCELLED);
            return save(schedule);
        }
        return null;
    }

    @Override
    public Schedule completeSchedule(Long scheduleId) {
        Schedule schedule = findById(scheduleId);
        if (schedule != null) {
            schedule.setStatus(ScheduleStatus.COMPLETED);
            return save(schedule);
        }
        return null;
    }

    // Các phương thức cho TeacherScheduleController
    @Override
    public List<Schedule> findTeacherScheduleByWeek(Long teacherId, LocalDate startDate, LocalDate endDate) {
        return scheduleRepository.findTeacherScheduleByWeek(teacherId, startDate, endDate);
    }

    @Override
    public List<Schedule> findPendingSchedulesByTeacher(Long teacherId) {
        return scheduleRepository.findPendingSchedulesByTeacher(teacherId);
    }

    @Override
    public List<Schedule> findTeacherScheduleByMonth(Long teacherId, int year, int month) {
        // Tính toán ngày đầu và cuối tháng
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        return findTeacherScheduleByWeek(teacherId, startDate, endDate);
    }

    @Override
    public List<Schedule> findTeacherScheduleByPeriod(Long teacherId, LocalDate startDate, LocalDate endDate) {
        return findTeacherScheduleByWeek(teacherId, startDate, endDate);
    }

    // Các phương thức cho StudentScheduleController
    @Override
    public List<Schedule> findStudentScheduleByWeek(Long studentId, LocalDate startDate, LocalDate endDate) {
        return scheduleRepository.findStudentScheduleByWeek(studentId, startDate, endDate);
    }

    @Override
    public List<Schedule> findAvailableSchedulesForRegistration() {
        return scheduleRepository.findAvailableSchedulesForRegistration();
    }

    @Override
    public List<Schedule> findAvailableSchedulesBySubject(Long subjectId) {
        return scheduleRepository.findAvailableSchedulesBySubject(subjectId);
    }

    @Override
    public boolean hasStudentScheduleConflict(Long studentId, Integer dayOfWeek, Long timeSlotId, 
                                            LocalDate startDate, LocalDate endDate) {
        List<Schedule> conflicts = scheduleRepository.findStudentScheduleConflicts(
            studentId, dayOfWeek, timeSlotId, startDate, endDate);
        return !conflicts.isEmpty();
    }

    @Override
    public boolean registerStudentForSchedule(Long studentId, Long scheduleId) {
        try {
            int result = scheduleRepository.registerStudentForSchedule(studentId, scheduleId);
            return result > 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean unregisterStudentFromSchedule(Long studentId, Long scheduleId) {
        try {
            int result = scheduleRepository.unregisterStudentFromSchedule(studentId, scheduleId);
            return result > 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<String> checkScheduleConflicts(Schedule schedule) {
        List<String> conflicts = new java.util.ArrayList<>();
        
        if (schedule.getTeacher() != null && schedule.getTimeSlot() != null) {
            boolean teacherConflict = hasTeacherConflict(
                schedule.getTeacher().getId(),
                schedule.getDayOfWeek() != null ? schedule.getDayOfWeek().ordinal() : null,
                schedule.getTimeSlot().getId(),
                schedule.getStartDate(),
                schedule.getEndDate()
            );
            if (teacherConflict) {
                conflicts.add("Giảng viên đã có lịch dạy vào thời gian này");
            }
        }
        
        if (schedule.getRoom() != null && schedule.getTimeSlot() != null) {
            boolean roomConflict = hasRoomConflict(
                schedule.getRoom().getId(),
                schedule.getDayOfWeek() != null ? schedule.getDayOfWeek().ordinal() : null,
                schedule.getTimeSlot().getId(),
                schedule.getStartDate(),
                schedule.getEndDate()
            );
            if (roomConflict) {
                conflicts.add("Phòng học đã được sử dụng vào thời gian này");
            }
        }
        
        if (schedule.getClassEntity() != null && schedule.getTimeSlot() != null) {
            boolean classConflict = hasClassConflict(
                schedule.getClassEntity().getId(),
                schedule.getDayOfWeek() != null ? schedule.getDayOfWeek().ordinal() : null,
                schedule.getTimeSlot().getId(),
                schedule.getStartDate(),
                schedule.getEndDate()
            );
            if (classConflict) {
                conflicts.add("Lớp học đã có lịch học vào thời gian này");
            }
        }
        
        return conflicts;
    }

    @Override
    public Schedule saveWithConflictCheck(Schedule schedule) throws IllegalStateException {
        // Kiểm tra conflict trước khi save
        List<String> conflicts = checkScheduleConflicts(schedule);
        
        if (!conflicts.isEmpty()) {
            throw new IllegalStateException("Scheduling conflicts detected: " + String.join(", ", conflicts));
        }
        
        return save(schedule);
    }

    @Override
    public boolean hasTeacherConflict(Long teacherId, Integer dayOfWeek, Long timeSlotId, 
                                     LocalDate startDate, LocalDate endDate) {
        List<Schedule> conflicts = findConflictingTeacherSchedules(teacherId, dayOfWeek, timeSlotId, startDate, endDate);
        return !conflicts.isEmpty();
    }

    @Override
    public boolean hasRoomConflict(Long roomId, Integer dayOfWeek, Long timeSlotId, 
                                  LocalDate startDate, LocalDate endDate) {
        List<Schedule> conflicts = findConflictingRoomSchedules(roomId, dayOfWeek, timeSlotId, startDate, endDate);
        return !conflicts.isEmpty();
    }

    @Override
    public boolean hasClassConflict(Long classId, Integer dayOfWeek, Long timeSlotId, 
                                   LocalDate startDate, LocalDate endDate) {
        List<Schedule> conflicts = findConflictingClassSchedules(classId, dayOfWeek, timeSlotId, startDate, endDate);
        return !conflicts.isEmpty();
    }
}

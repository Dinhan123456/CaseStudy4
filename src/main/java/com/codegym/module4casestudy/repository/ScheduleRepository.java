package com.codegym.module4casestudy.repository;

import com.codegym.module4casestudy.model.Schedule;
import com.codegym.module4casestudy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    
    // Tìm lịch theo giảng viên
    List<Schedule> findByTeacherAndActiveTrue(User teacher);
    
    // Tìm lịch theo lớp học
    @Query("SELECT s FROM Schedule s WHERE s.classEntity.id = :classId AND s.active = true")
    List<Schedule> findByClassId(@Param("classId") Long classId);
    
    // Tìm lịch theo môn học
    @Query("SELECT s FROM Schedule s WHERE s.subject.id = :subjectId AND s.active = true")
    List<Schedule> findBySubjectId(@Param("subjectId") Long subjectId);
    
    // Tìm lịch theo phòng học
    @Query("SELECT s FROM Schedule s WHERE s.room.id = :roomId AND s.active = true")
    List<Schedule> findByRoomId(@Param("roomId") Long roomId);
    
    // Tìm lịch theo ngày trong tuần
    List<Schedule> findByDayOfWeekAndActiveTrue(Integer dayOfWeek);
    
    // Tìm lịch theo giảng viên và ngày
    @Query("SELECT s FROM Schedule s WHERE s.teacher.id = :teacherId AND s.dayOfWeek = :dayOfWeek AND s.active = true")
    List<Schedule> findByTeacherAndDay(@Param("teacherId") Long teacherId, @Param("dayOfWeek") Integer dayOfWeek);
    
    // Tìm lịch theo lớp và ngày
    @Query("SELECT s FROM Schedule s WHERE s.classEntity.id = :classId AND s.dayOfWeek = :dayOfWeek AND s.active = true")
    List<Schedule> findByClassAndDay(@Param("classId") Long classId, @Param("dayOfWeek") Integer dayOfWeek);
    
    // Kiểm tra trùng lịch giảng viên
    @Query("SELECT s FROM Schedule s WHERE s.teacher.id = :teacherId AND s.dayOfWeek = :dayOfWeek " +
           "AND s.timeSlot.id = :timeSlotId AND s.active = true " +
           "AND (s.startDate <= :endDate AND (s.endDate IS NULL OR s.endDate >= :startDate))")
    List<Schedule> findConflictingTeacherSchedules(@Param("teacherId") Long teacherId,
                                                   @Param("dayOfWeek") Integer dayOfWeek,
                                                   @Param("timeSlotId") Long timeSlotId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);
    
    // Kiểm tra trùng lịch phòng học
    @Query("SELECT s FROM Schedule s WHERE s.room.id = :roomId AND s.dayOfWeek = :dayOfWeek " +
           "AND s.timeSlot.id = :timeSlotId AND s.active = true " +
           "AND (s.startDate <= :endDate AND (s.endDate IS NULL OR s.endDate >= :startDate))")
    List<Schedule> findConflictingRoomSchedules(@Param("roomId") Long roomId,
                                                @Param("dayOfWeek") Integer dayOfWeek,
                                                @Param("timeSlotId") Long timeSlotId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);

    @Query("""
        SELECT s FROM Schedule s
        JOIN FETCH s.classEntity c
        JOIN FETCH s.subject subj
        JOIN FETCH s.timeSlot ts
        LEFT JOIN FETCH s.teacher t
        LEFT JOIN FETCH s.room r
        JOIN c.students st
        WHERE st.id = :studentId
          AND s.active = true
          AND s.startDate <= :date
          AND (s.endDate IS NULL OR s.endDate >= :date)
    """)
    List<Schedule> findForStudentOnDateWithJoins(@Param("studentId") Long studentId,
                                                 @Param("date") LocalDate date);

    @Query("""
        SELECT s FROM Schedule s
        JOIN FETCH s.classEntity c
        JOIN FETCH s.subject subj
        JOIN FETCH s.timeSlot ts
        LEFT JOIN FETCH s.teacher t
        LEFT JOIN FETCH s.room r
        JOIN c.students st
        WHERE st.id = :studentId
          AND s.active = true
          AND s.startDate <= :endDate
          AND (s.endDate IS NULL OR s.endDate >= :startDate)
    """)
    List<Schedule> findForStudentBetweenWithJoins(@Param("studentId") Long studentId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    // Kiểm tra trùng lịch lớp học
    @Query("SELECT s FROM Schedule s WHERE s.classEntity.id = :classId AND s.dayOfWeek = :dayOfWeek " +
           "AND s.timeSlot.id = :timeSlotId AND s.active = true " +
           "AND (s.startDate <= :endDate AND (s.endDate IS NULL OR s.endDate >= :startDate))")
    List<Schedule> findConflictingClassSchedules(@Param("classId") Long classId,
                                                 @Param("dayOfWeek") Integer dayOfWeek,
                                                 @Param("timeSlotId") Long timeSlotId,
                                                 @Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);
    
    // Tìm lịch trong khoảng thời gian
    @Query("SELECT s FROM Schedule s WHERE s.active = true " +
           "AND (s.startDate <= :endDate AND (s.endDate IS NULL OR s.endDate >= :startDate))")
    List<Schedule> findSchedulesInDateRange(@Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);
    
    // Tìm lịch theo trạng thái
    @Query("SELECT s FROM Schedule s WHERE s.status = :status AND s.active = true")
    List<Schedule> findByStatus(@Param("status") Schedule.ScheduleStatus status);
    
    // Tìm lịch cho sinh viên (thông qua lớp họ đăng ký)
    @Query("SELECT DISTINCT s FROM Schedule s " +
           "JOIN s.classEntity c " +
           "JOIN c.students st " +
           "WHERE st.id = :studentId AND s.active = true")
    List<Schedule> findByStudentId(@Param("studentId") Long studentId);
    
    // Lấy lịch toàn hệ thống cho admin
    @Query("SELECT s FROM Schedule s WHERE s.active = true ORDER BY s.dayOfWeek, s.timeSlot.periodNumber")
    List<Schedule> findAllActiveSchedules();
    
    // Các phương thức cho TeacherScheduleController
    @Query("SELECT s FROM Schedule s WHERE s.teacher.id = :teacherId AND s.active = true " +
           "AND (s.startDate <= :endDate AND (s.endDate IS NULL OR s.endDate >= :startDate)) " +
           "ORDER BY s.dayOfWeek, s.timeSlot.periodNumber")
    List<Schedule> findTeacherScheduleByWeek(@Param("teacherId") Long teacherId,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);
    
    @Query("SELECT s FROM Schedule s WHERE s.teacher.id = :teacherId AND s.status = 'PENDING' AND s.active = true")
    List<Schedule> findPendingSchedulesByTeacher(@Param("teacherId") Long teacherId);
    
    // Các phương thức cho StudentScheduleController
    @Query("SELECT DISTINCT s FROM Schedule s " +
           "JOIN s.classEntity c " +
           "JOIN c.students st " +
           "WHERE st.id = :studentId AND s.active = true " +
           "AND (s.startDate <= :endDate AND (s.endDate IS NULL OR s.endDate >= :startDate)) " +
           "ORDER BY s.dayOfWeek, s.timeSlot.periodNumber")
    List<Schedule> findStudentScheduleByWeek(@Param("studentId") Long studentId,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);
    
    @Query("SELECT s FROM Schedule s WHERE s.status = 'CONFIRMED' AND s.active = true " +
           "AND s.startDate >= CURRENT_DATE " +
           "ORDER BY s.subject.name, s.dayOfWeek, s.timeSlot.periodNumber")
    List<Schedule> findAvailableSchedulesForRegistration();
    
    @Query("SELECT s FROM Schedule s WHERE s.subject.id = :subjectId " +
           "AND s.status = 'CONFIRMED' AND s.active = true " +
           "AND s.startDate >= CURRENT_DATE " +
           "ORDER BY s.dayOfWeek, s.timeSlot.periodNumber")
    List<Schedule> findAvailableSchedulesBySubject(@Param("subjectId") Long subjectId);
    
    @Query("SELECT s FROM Schedule s " +
           "JOIN s.classEntity c " +
           "JOIN c.students st " +
           "WHERE st.id = :studentId AND s.dayOfWeek = :dayOfWeek " +
           "AND s.timeSlot.id = :timeSlotId AND s.active = true " +
           "AND (s.startDate <= :endDate AND (s.endDate IS NULL OR s.endDate >= :startDate))")
    List<Schedule> findStudentScheduleConflicts(@Param("studentId") Long studentId,
                                               @Param("dayOfWeek") Integer dayOfWeek,
                                               @Param("timeSlotId") Long timeSlotId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);



    // Phương thức đăng ký sinh viên vào lớp (cần custom implementation)
    @Modifying
    @Query(value = "INSERT INTO class_students (class_id, student_id) " +
                   "SELECT s.class_id, :studentId FROM schedules s " +
                   "WHERE s.id = :scheduleId AND s.active = true " +
                   "AND NOT EXISTS (SELECT 1 FROM class_students cs " +
                   "WHERE cs.class_id = s.class_id AND cs.student_id = :studentId)", 
           nativeQuery = true)
    int registerStudentForSchedule(@Param("studentId") Long studentId, 
                                   @Param("scheduleId") Long scheduleId);
    
    @Modifying
    @Query(value = "DELETE FROM class_students cs " +
                   "WHERE cs.student_id = :studentId " +
                   "AND cs.class_id = (SELECT s.class_id FROM schedules s WHERE s.id = :scheduleId)", 
           nativeQuery = true)
    int unregisterStudentFromSchedule(@Param("studentId") Long studentId, 
                                      @Param("scheduleId") Long scheduleId);
}
